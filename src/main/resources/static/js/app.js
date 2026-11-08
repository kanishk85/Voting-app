document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-toggle-password]').forEach((button) => {
    button.addEventListener('click', () => {
      const input = document.getElementById(button.dataset.togglePassword);
      if (!input) return;
      const revealing = input.type === 'password';
      input.type = revealing ? 'text' : 'password';
      button.textContent = revealing ? 'Hide' : 'Show';
      button.setAttribute('aria-label', `${revealing ? 'Hide' : 'Show'} password`);
    });
  });

  const ballot = document.getElementById('ballotForm');
  const voteButton = document.getElementById('voteButton');
  if (ballot && voteButton) {
    ballot.querySelectorAll('input[name="candidateId"]').forEach((radio) => {
      radio.addEventListener('change', () => { voteButton.disabled = false; });
    });
    ballot.addEventListener('submit', (event) => {
      if (!ballot.querySelector('input[name="candidateId"]:checked')) {
        event.preventDefault();
        return;
      }
      voteButton.disabled = true;
      voteButton.innerHTML = 'Recording your vote… <span class="arrow-mark">↗</span>';
    });
  }

  const activeBar = document.querySelector('.progress-fill');
  if (activeBar) requestAnimationFrame(() => activeBar.classList.add('progress-fill-ready'));
});
