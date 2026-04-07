document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('search-input');
    const searchBtn = document.getElementById('search-btn');
    const searchResultsDiv = document.getElementById('search-results');
    const pendingRequestsDiv = document.getElementById('pending-requests');
    const friendsListDiv = document.getElementById('friends-list'); // ✅ NEW

    // --- Event Listeners ---
    searchBtn.addEventListener('click', handleSearch);
    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') handleSearch();
    });

    searchResultsDiv.addEventListener('click', handleActionClick);
    pendingRequestsDiv.addEventListener('click', handleActionClick);

    // --- Core Functions ---
    async function handleSearch() {
        // ... (this function remains the same)
    }

    async function loadPendingRequests() {
        // ... (this function remains the same)
    }

    // ✅ NEW: Function to load the current friends list
    async function loadFriends() {
        try {
            const response = await fetch('/api/friendships/accepted');
            if (!response.ok) throw new Error('Failed to load friends');
            const friendships = await response.json();
            renderFriendsList(friendships);
        } catch (error) {
            console.error('Error loading friends:', error);
            friendsListDiv.innerHTML = '<p class="error">Could not load friends list.</p>';
        }
    }

    // --- Action Handlers ---
    async function handleActionClick(event) {
        // ... (this function remains the same)
    }
    // ... (sendFriendRequest, acceptFriendRequest, declineFriendRequest functions remain the same)

    // --- Rendering Functions ---
    function renderSearchResults(users) {
        // ... (this function remains the same)
    }

    function renderPendingRequests(requests) {
        // ... (this function remains the same)
    }

    // ✅ NEW: Function to render the friends list
    function renderFriendsList(friendships) {
        if (friendships.length === 0) {
            friendsListDiv.innerHTML = '<p>You haven\'t added any friends yet.</p>';
            return;
        }
        // We need to figure out the current user's ID to display the other person's name
        // This is a simplified way; in a real app, the session or a global object might hold this.
        const currentUserId = /* This part needs the current user's ID from the session, which we can't get directly here.
                                 For now, the backend will send the full friendship object. */
            ''; // Placeholder

        friendsListDiv.innerHTML = friendships.map(friendship => {
            // Determine who the "friend" is in the relationship object
            const friend = friendship.requester.id === currentUserId ? friendship.addressee : friendship.requester;
            return `
                <div class="user-item">
                    <span class="user-info">${friend.username}</span>
                    <div class="user-actions">
                        </div>
                </div>
            `;
        }).join('');
    }


    // --- Initial Page Load ---
    loadPendingRequests();
    loadFriends(); // ✅ NEW: Load friends when the page opens
});