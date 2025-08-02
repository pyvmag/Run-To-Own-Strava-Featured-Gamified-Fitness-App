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

  // ===== Weekly Chart with ApexCharts =====
  fetch('/strava/activities/all')
    .then(res => res.json())
    .then(activities => {
      if (!activities || activities.length === 0) return;

      const currentYear = new Date().getFullYear();
      let totalDistance = 0, totalTime = 0, totalElevation = 0;

      function getWeekKey(date) {
        const start = new Date(date.getFullYear(), 0, 1);
        const diff = date - start + ((start.getTimezoneOffset() - date.getTimezoneOffset()) * 60000);
        return `Week ${Math.floor(diff / (7 * 24 * 60 * 60 * 1000)) + 1}`;
      }

      const runWeekly = {}, weightWeekly = {};
      const runs = activities.filter(act => act.type === 'Run');

      activities.forEach(act => {
        const date = new Date(act.start_date);
        if (date.getFullYear() !== currentYear) return;

        const week = getWeekKey(date);
        const hours = (act.elapsed_time || 0) / 3600;

        if (act.type === 'Run') {
          totalDistance += act.distance || 0;
          totalTime += act.elapsed_time || 0;
          totalElevation += act.total_elevation_gain || 0;
          runWeekly[week] = (runWeekly[week] || 0) + hours;
        }

        if (act.type === 'WeightTraining' || act.type === 'Workout') {
          weightWeekly[week] = (weightWeekly[week] || 0) + hours;
        }
      });

      // Update stat cards
      document.getElementById('total-distance').innerText = (totalDistance / 1000).toFixed(1) + ' km';
      document.getElementById('total-runs').innerText = runs.length;
      document.getElementById('total-time').innerText = (totalTime / 3600).toFixed(1) + ' h';
      document.getElementById('total-elevation').innerText = totalElevation.toFixed(1) + ' m';

      function toApexSeries(data) {
        return Object.entries(data)
          .sort(([a], [b]) => parseInt(a.split(' ')[1]) - parseInt(b.split(' ')[1]))
          .map(([week, value]) => ({ x: week, y: parseFloat(value.toFixed(2)) }));
      }

      const runSeries = toApexSeries(runWeekly);
      const weightSeries = toApexSeries(weightWeekly);
      const chartOptions = {
        chart: {
          type: 'line',
          height: 300,
          zoom: { enabled: true },
          toolbar: { show: false }
        },
        tooltip: {
          theme: 'dark',
          style: {
            fontSize: '14px',
            fontFamily: 'Segoe UI',
            color: '#fff'
          },
          fillSeriesColor: false,
          custom: function({ series, seriesIndex, dataPointIndex, w }) {
            const week = w.globals.categoryLabels[dataPointIndex];
            const value = series[seriesIndex][dataPointIndex].toFixed(2);
            return `<div style="padding:8px; background:#222; color:#fc4c02; border-radius:6px;">
                      <strong>${week}</strong><br/>${value} hrs
                    </div>`;
          },
          x: { show: true },
          y: {
            formatter: val => `${val.toFixed(2)} hrs`
          }
        },
        dataLabels: { enabled: false },
        stroke: {
          curve: 'smooth',
          width: 3
        },
        xaxis: {
          type: 'category',
          title: { text: 'Week' },
          labels: { rotate: 0 }
        },
        yaxis: {
          title: { text: 'Hours per Week' },
          min: 0
        },
        series: [{ name: 'Run Hours', data: runSeries }],
        colors: ['#fc4c02']
      };

      const chart = new ApexCharts(document.querySelector('#weeklyChart'), chartOptions);
      chart.render();

      const runBtn = document.getElementById('toggle-run');
      const weightBtn = document.getElementById('toggle-weight');

      runBtn.addEventListener('click', () => {
        runBtn.classList.add('active');
        weightBtn.classList.remove('active');
        chart.updateSeries([{ name: 'Run Hours', data: runSeries }]);
        chart.updateOptions({ colors: ['#fc4c02'] });
      });

      weightBtn.addEventListener('click', () => {
        weightBtn.classList.add('active');
        runBtn.classList.remove('active');
        chart.updateSeries([{ name: 'Weight Training', data: weightSeries }]);
        chart.updateOptions({ colors: ['#0077cc'] });
      });
    })
    .catch(err => console.error("Error fetching activities:", err));
});