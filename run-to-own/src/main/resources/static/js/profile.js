document.addEventListener('DOMContentLoaded', () => {
  // ===== Athlete Profile =====
  fetch('/strava/athlete')
    .then(res => res.json())
    .then(athlete => {
      if (!athlete) return;
      document.getElementById('athlete-name').innerText = `${athlete.firstname || ''} ${athlete.lastname || ''}`;
      document.getElementById('athlete-location').innerText = [athlete.city, athlete.country].filter(Boolean).join(', ') || 'Unknown';

      const avatar = document.getElementById('profile-avatar');
      avatar.src = athlete.profile || '/images/default-avatar.png';
      avatar.alt = `${athlete.firstname || 'Athlete'} Profile Picture`;

      if (athlete.premium) {
        document.getElementById('premium-status').style.display = 'inline-block';
      }
    })
    .catch(err => console.error('Failed to fetch athlete:', err));

  // ===== Athlete Gear =====
  fetch('/strava/profile/gear')
    .then(res => res.json())
    .then(gear => {
      const grid = document.getElementById('gear-grid');
      grid.innerHTML = '';

      if (!gear || gear.length === 0) {
        grid.innerHTML = '<p>No gear found</p>';
        return;
      }

      gear.sort((a, b) => (b.distance || 0) - (a.distance || 0));

      gear.forEach(g => {
        const km = ((g.distance || 0) / 1000).toFixed(1);
        const div = document.createElement('div');
        div.className = 'gear-card';

        div.innerHTML = `
          <h3>${g.name}</h3>
          <p>Used: ${km} km</p>
          <div class="gear-distance-bar" style="width: ${Math.min(km / 10, 100)}%;"></div>
          ${g.retired ? '<div class="retired-stamp">Retired</div>' : ''}
        `;

        grid.appendChild(div);
      });
    })
    .catch(err => console.error("Error fetching gear:", err));

  // ===== Athlete Activities and Charts =====
  fetch('/strava/activities?per_page=100')
    .then(res => {
      if (!res.ok) throw new Error("Failed to load activities");
      return res.json();
    })
    .then(activities => {
      if (!activities || activities.length === 0) return;

      const runs = activities.filter(act => act.type === 'Run');
      let totalDistance = 0;
      let totalTime = 0;
      let totalElevation = 0;

      const weeklyRun = [0, 0, 0, 0, 0, 0, 0];
      const weeklyWeight = [0, 0, 0, 0, 0, 0, 0];

      runs.forEach(run => {
        const day = new Date(run.start_date).getDay();
        totalDistance += run.distance || 0;
        totalTime += run.elapsed_time || 0;
        totalElevation += run.total_elevation_gain || 0;
        weeklyRun[day] += (run.distance || 0) / 1000;
      });

      activities.forEach(act => {
        const day = new Date(act.start_date).getDay();
        if (act.type === 'WeightTraining' || act.type === 'Workout') {
          weeklyWeight[day] += (act.elapsed_time || 0) / 60;
        }
      });

      document.getElementById('total-distance').innerText = (totalDistance / 1000).toFixed(1) + ' km';
      document.getElementById('total-runs').innerText = runs.length;
      document.getElementById('total-time').innerText = (totalTime / 3600).toFixed(1) + ' h';
      document.getElementById('total-elevation').innerText = totalElevation.toFixed(1) + ' m';

      // ===== Chart Setup =====
      const ctx = document.getElementById('weeklyChart').getContext('2d');
      let currentChart;

      function renderChart(label, data, color) {
        if (currentChart) currentChart.destroy();

        currentChart = new Chart(ctx, {
          type: 'bar',
          data: {
            labels: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
            datasets: [{
              label,
              data,
              backgroundColor: color,
              borderRadius: 5
            }]
          },
          options: {
            responsive: true,
            plugins: { legend: { display: false } }
          }
        });
      }

      // Initial chart render
      renderChart('Running Distance (km)', weeklyRun, '#fc4c02');

      // Toggle buttons
      const runBtn = document.getElementById('toggle-run');
      const weightBtn = document.getElementById('toggle-weight');

      runBtn.addEventListener('click', () => {
        runBtn.classList.add('active');
        weightBtn.classList.remove('active');
        renderChart('Running Distance (km)', weeklyRun, '#fc4c02');
      });

      weightBtn.addEventListener('click', () => {
        weightBtn.classList.add('active');
        runBtn.classList.remove('active');
        renderChart('Weight Training (min)', weeklyWeight, '#0077cc');
      });
    })
    .catch(err => console.error("Error fetching activities:", err));
});