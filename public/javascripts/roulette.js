$(document).ready(function() {
    let selectedPlayer = 0;
    let betType = 'n';
    let betValue = '0';

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
    
        $.ajax({
            url: '/api/bet',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                playerIndex: selectedPlayer,
                betType: betType,
                betValue: betType === 'n' ? betNumber : betValue,
                betAmount: parseInt(betAmount)
            }),
            success: function(data) {
                updatePlayerMoney(data.players);
                $('#state-label').text('Wette platziert!');
            },
            error: function(xhr, status, error) {
                console.error('Error:', error);
                $('#state-label').text('Fehler beim Platzieren der Wette.');
            }
        });
    }

    // Wettphase beenden
    $('#end-betting').click(function() {
        const wheel = $('#wheel-image');
        wheel.css('transition', 'transform 5s cubic-bezier(0.25, 0.1, 0.25, 1)');
        wheel.css('transform', `rotate(${Math.random() * 360 * 5}deg)`);

        $.ajax({
            url: '/api/calculateBets',
            method: 'GET',
            success: function(data) {
                setTimeout(function() {
                    $('#result').removeClass('d-none');
                    $('#result-text').text(data.results[0]);
                    $('#state-label').text('Runde beendet!');
                    updatePlayerMoney(data.players);
                }, 5000);
            },
            error: function(xhr, status, error) {
                console.error('Error:', error);
                $('#state-label').text('Fehler beim Berechnen der Wetten.');
            }
        });
    });

    // Spielerguthaben aktualisieren
    function updatePlayerMoney(players) {
        if (players && players.length >= 2) {
            $('#player-one-money').text(`P1: ${players[0].available_money}$`);
            $('#player-two-money').text(`P2: ${players[1].available_money}$`);
        } else {
            console.error('Ungültige Spielerdaten:', players);
        }
    }

    // Undo und Redo
    $('#undo, #redo').click(function() {
        const action = $(this).attr('id');
        $.ajax({
            url: `/api/${action}`,
            method: 'POST',
            success: function(data) {
                updatePlayerMoney(data.players);
            },
            error: function(xhr, status, error) {
                console.error('Error:', error);
            }
        });
    });

    // Spielstand speichern und laden
    $('#saveDB, #loadDB, #saveFile, #loadFile').click(function() {
        const action = $(this).attr('id');
        $.ajax({
            url: `/api/${action}`,
            method: 'POST',
            success: function(data) {
                updatePlayerMoney(data.players);
                $('#state-label').text(`Spielstand erfolgreich ${action === 'saveDB' || action === 'saveFile' ? 'gespeichert' : 'geladen'}.`);
            },
            error: function(xhr, status, error) {
                console.error('Error:', error);
                $('#state-label').text(`Fehler beim ${action === 'saveDB' || action === 'saveFile' ? 'Speichern' : 'Laden'} des Spielstands.`);
            }
        });
    });
});