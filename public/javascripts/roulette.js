$(document).ready(function() {
    let selectedPlayer = 0;
    let betType = 'n';
    let betValue = '0';
    let socket;

    function connectWebSocket() {
        socket = new WebSocket("ws://" + window.location.host + "/socket");

        socket.onopen = function() {
            console.log("WebSocket verbunden");
        };

        socket.onmessage = function(message) {
            const data = JSON.parse(message.data);
            handleSocketMessage(data);
        };

        socket.onerror = function(error) {
            console.error("WebSocket Fehler:", error);
        };

        socket.onclose = function() {
            console.log("WebSocket geschlossen. Versuche erneut zu verbinden...");
            setTimeout(connectWebSocket, 5000);
        };
    }

    connectWebSocket();

    function handleSocketMessage(data) {
        switch(data.action) {
            case "betPlaced":
            case "betsCalculated":
                updatePlayerMoney(data.players);
                if (data.action === "betsCalculated" && data.results) {
                    $('#result').removeClass('d-none');
                    $('#result-text').text(data.results[0]);
                    $('#state-label').text('Runde beendet!');
                }
                break;
            case "spinWheel":
                rotateWheel(data.rotation);
                break;
            case "error":
                console.error('Fehler:', data.message);
                $('#state-label').text('Fehler: ' + data.message);
                break;
        }
    }

    // Spielerauswahl
    $('#player-selection button').click(function() {
        selectedPlayer = $(this).index();
        $(this).addClass('active').siblings().removeClass('active');
    });

    // Wetttyp-Auswahl
    $('#bet-options button').click(function() {
        betType = $(this).data('bet-type');
        betValue = $(this).data('bet-value') || '';
        $('#bet-number-input').prop('disabled', betType !== 'n');
        $(this).addClass('active').siblings().removeClass('active');
    });

    $('#place-bet').click(placeBet);

    function placeBet() {
        const betAmount = $('#bet-amount-input').val();
        const betNumber = $('#bet-number-input').val();
    
        socket.send(JSON.stringify({
            action: "placeBet",
            playerIndex: selectedPlayer,
            betType: betType,
            betValue: betType === 'n' ? betNumber : betValue,
            betAmount: parseInt(betAmount)
        }));

        $('#state-label').text('Wette platziert!');
    }

    // Wettphase beenden
    function spinWheel() {
        const rotation = Math.random() * 360 * 5;
        socket.send(JSON.stringify({
            action: "spinWheel",
            rotation: rotation
        }));
    }
    
    $('#end-betting').click(function() {
        spinWheel();
        socket.send(JSON.stringify({action: "calculateBets"}));
    });
    
    function rotateWheel(rotation) {
        const wheel = $('#wheel-image');
        wheel.css('transition', 'transform 5s cubic-bezier(0.25, 0.1, 0.25, 1)');
        wheel.css('transform', `rotate(${rotation}deg)`);
    }

    // Spielerguthaben aktualisieren
    function updatePlayerMoney(players) {
        if (players && players.length >= 2) {
            $('#player-one-money').text(`P1: ${players[0].available_money}$`);
            $('#player-two-money').text(`P2: ${players[1].available_money}$`);
        } else {
            console.error('Ungültige Spielerdaten:', players);
        }
    }
});