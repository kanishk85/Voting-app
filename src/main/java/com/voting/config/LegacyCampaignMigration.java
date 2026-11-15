package com.voting.config;

import com.voting.entity.Campaign;
import com.voting.repository.CampaignRepository;
import com.voting.repository.CandidateRepository;
import com.voting.repository.VoteRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LegacyCampaignMigration implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final CampaignRepository campaigns;
    private final CandidateRepository candidates;
    private final VoteRepository votes;

    public LegacyCampaignMigration(JdbcTemplate jdbc, CampaignRepository campaigns,
                                   CandidateRepository candidates, VoteRepository votes) {
        this.jdbc = jdbc;
        this.campaigns = campaigns;
        this.candidates = candidates;
        this.votes = votes;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean oldElectionTable = tableExists("elections");
        var unassignedCandidates = candidates.findAllByCampaignIsNullOrderByIdAsc();
        Campaign legacyCampaign = campaigns.findAllByOrderByStartDateDesc().stream().findFirst().orElse(null);

        if (!unassignedCandidates.isEmpty() && legacyCampaign == null) {
            legacyCampaign = createLegacyCampaign(oldElectionTable);
        }
        if (legacyCampaign != null) {
            for (var candidate : unassignedCandidates) {
                candidate.setCampaign(legacyCampaign);
            }
            candidates.saveAll(unassignedCandidates);
            candidates.flush();
            var unassignedVotes = votes.findAllByCampaignIsNull();
            for (var vote : unassignedVotes) {
                vote.setCampaign(vote.getCandidate().getCampaign() == null ? legacyCampaign : vote.getCandidate().getCampaign());
            }
            votes.saveAll(unassignedVotes);
            votes.flush();
        }

        dropOldVoterOnlyUniqueIndex();
        makeCampaignReferencesRequired("candidates");
        makeCampaignReferencesRequired("votes");
        dropColumnIfExists("candidates", "vote_count");
        dropColumnIfExists("app_users", "voted");
        if (oldElectionTable) jdbc.execute("DROP TABLE elections");
    }

    private Campaign createLegacyCampaign(boolean oldElectionTable) {
        if (oldElectionTable) {
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT title, starts_at, ends_at FROM elections ORDER BY id LIMIT 1");
            if (!rows.isEmpty()) {
                String name = String.valueOf(rows.getFirst().get("title"));
                LocalDate start = toDate(rows.getFirst().get("starts_at"));
                LocalDate end = toDate(rows.getFirst().get("ends_at"));
                return campaigns.saveAndFlush(new Campaign(name, "Migrated election", start, end));
            }
        }
        LocalDate today = LocalDate.now();
        return campaigns.saveAndFlush(new Campaign("Community Election", "Migrated election", today.minusDays(1), today.plusDays(365)));
    }

    private LocalDate toDate(Object value) {
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (value instanceof java.util.Date date) return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (value instanceof Instant instant) return instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return LocalDate.parse(String.valueOf(value).substring(0, 10));
    }

    private boolean tableExists(String name) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?", Integer.class, name);
        return count != null && count > 0;
    }

    private void dropOldVoterOnlyUniqueIndex() {
        // A fresh database has no legacy votes table to inspect. Hibernate creates
        // the current schema before this runner executes, but guard this migration
        // so it also remains safe if schema creation is deferred or partially done.
        if (!tableExists("votes")) return;
        List<Map<String, Object>> rows = jdbc.queryForList("SHOW INDEX FROM votes WHERE Non_unique = 0");
        Map<String, List<String>> columnsByIndex = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String key = String.valueOf(row.get("Key_name"));
            if (!"PRIMARY".equalsIgnoreCase(key)) columnsByIndex.computeIfAbsent(key, ignored -> new ArrayList<>()).add(String.valueOf(row.get("Column_name")));
        }
        for (var entry : columnsByIndex.entrySet()) {
            if (entry.getValue().equals(List.of("voter_id"))) {
                String indexName = entry.getKey().replace("`", "``");
                jdbc.execute("ALTER TABLE votes DROP INDEX `" + indexName + "`");
            }
        }
    }

    private void makeCampaignReferencesRequired(String table) {
        if (!tableExists(table) || !columnExists(table, "campaign_id")) return;
        Integer nullCount = jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE campaign_id IS NULL", Integer.class);
        if (nullCount != null && nullCount == 0) {
            jdbc.execute("ALTER TABLE " + table + " MODIFY campaign_id BIGINT NOT NULL");
        }
    }

    private void dropColumnIfExists(String table, String column) {
        if (columnExists(table, column)) jdbc.execute("ALTER TABLE " + table + " DROP COLUMN " + column);
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?", Integer.class, table, column);
        return count != null && count > 0;
    }
}
