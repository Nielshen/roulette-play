let selectedPlayer = 0;
let betType = 'n';
let betValue = '0';

function selectPlayer(playerIndex) {
    selectedPlayer = playerIndex;
}

function selectBetType(type, value = '') {
    betType = type;
    betValue = value;
    document.getElementById('bet-number-input').disabled = (type !== 'n');
}

function placeBet() {
    const betAmount = document.getElementById('bet-amount-input').value;
    const betNumber = document.getElementById('bet-number-input').value;

    fetch('/api/bet', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            playerIndex: selectedPlayer,
            betType: betType,
            betValue: betType === 'n' ? betNumber : betValue,
            betAmount: parseInt(betAmount)
        }),
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('Received data:', data);
        if (data.players && Array.isArray(data.players)) {
            updatePlayerMoney(data.players);
        } else {
            console.error('Unexpected data format:', data);
        }
    })
    .catch((error) => {
        console.error('Error:', error);
    });
}

function endBettingPhase() {
    const wheel = document.getElementById('wheel-image');
    wheel.style.transition = 'transform 5s cubic-bezier(0.25, 0.1, 0.25, 1)';
    wheel.style.transform = `rotate(${Math.random() * 360 * 5}deg)`;

    fetch('/api/calculateBets')
    .then(response => response.json())
    .then(data => {
        setTimeout(() => {
            const resultElement = document.getElementById('result');
            resultElement.classList.remove('d-none');
            const winningNumber = data.results[0];
            const winAmount = data.results[1];
            document.getElementById('result-text').innerText = winningNumber;
            document.getElementById('state-label').innerText = 'Round ended!';
            updatePlayerMoney(data.players);
        }, 5000); // Warte 5 Sekunden bis das Rad aufhört sich zu drehen
    })
    .catch((error) => {
        console.error('Error:', error);
        document.getElementById('state-label').innerText = 'Fehler beim Berechnen der Wetten.';
    });
}

function updatePlayerMoney(players) {
    if (players && players.length >= 2) {
        document.getElementById('player-one-money').innerText = `P1: ${players[0].available_money}$`;
        document.getElementById('player-two-money').innerText = `P2: ${players[1].available_money}$`;
    } else {
        console.error('Invalid players data:', players);
    }
}

function undo() {
    fetch('/api/undo', { method: 'POST' })
    .then(response => response.json())
    .then(data => updatePlayerMoney(data.players))
    .catch(error => console.error('Error:', error));
}

function redo() {
    fetch('/api/redo', { method: 'POST' })
    .then(response => response.json())
    .then(data => updatePlayerMoney(data.players))
    .catch(error => console.error('Error:', error));
}