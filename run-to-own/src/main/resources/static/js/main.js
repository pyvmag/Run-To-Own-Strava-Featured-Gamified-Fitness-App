// main.js
document.addEventListener('DOMContentLoaded', () => {

    // --- Consolidated Theme Toggle Logic ---
    const themeBtn = document.getElementById('theme-switch');
    if (themeBtn) {
        // Function to apply the correct theme from memory
        const applyTheme = () => {
            const savedTheme = localStorage.getItem('theme');
            const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            if (savedTheme === 'dark' || (!savedTheme && systemPrefersDark)) {
                document.body.classList.add('dark');
            } else {
                document.body.classList.remove('dark');
            }
        };

        // Handle button click to toggle and save the theme
        themeBtn.addEventListener('click', () => {
            document.body.classList.toggle('dark');
            const currentTheme = document.body.classList.contains('dark') ? 'dark' : 'light';
            localStorage.setItem('theme', currentTheme);
        });

        // Apply the theme as soon as the page loads
        applyTheme();
    }

    // --- Streak Widget Logic ---
    const streakCounter = document.getElementById('streak-counter');
    const bestStreakCounter = document.getElementById('best-streak');
    if (streakCounter && bestStreakCounter) {
        async function loadStreak() {
            try {
                const response = await fetch('/api/user/details');
                if (!response.ok) return;
                const user = await response.json();

                if (user && user.currentStreak > 0) {
                    streakCounter.textContent = `${user.currentStreak} Day${user.currentStreak > 1 ? 's' : ''}`;
                } else {
                    streakCounter.textContent = '0 Days';
                }

                if (user && user.bestStreak > 0) {
                    bestStreakCounter.textContent = `Best: ${user.bestStreak} Day${user.bestStreak > 1 ? 's' : ''}`;
                } else {
                    bestStreakCounter.textContent = 'Best: 0 Days';
                }
            } catch (error) {
                console.error("Failed to load streak data:", error);
            }
        }
        loadStreak();
    }

    // --- Friends Dropdown Logic ---
    const dropdownBtn = document.getElementById('friends-dropdown-btn');
    if (dropdownBtn) {
        const dropdownMenu = document.getElementById('friends-dropdown-menu');
        const badge = document.getElementById('friend-request-badge');
        const requestList = document.getElementById('dropdown-request-list');

        dropdownBtn.addEventListener('click', (event) => {
            event.stopPropagation();
            dropdownMenu.classList.toggle('show');
        });

        window.addEventListener('click', () => {
            dropdownMenu.classList.remove('show');
        });

        async function loadFriendshipNotifications() {
            try {
                const response = await fetch('/api/friendships/requests/pending');
                if (!response.ok) return;
                const requests = await response.json();

                if (requests.length > 0) {
                    badge.textContent = requests.length;
                    badge.style.display = 'inline-block';
                    requestList.innerHTML = requests.slice(0, 3).map(req => `
                        <a href="/friends" class="dropdown-item">
                            <strong>${req.requester.username}</strong> sent you a request.
                        </a>
                    `).join('');
                } else {
                    requestList.innerHTML = '<div class="dropdown-item">No new requests</div>';
                }
            } catch (error) {
                console.error("Failed to load friend requests:", error);
            }
        }

        loadFriendshipNotifications();
    }
});