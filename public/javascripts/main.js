document.addEventListener('DOMContentLoaded', function() {
    const statusText = document.getElementById('statusText');
    const ergebnisText = document.getElementById('ergebnisText');
    const ergebnisDiv = document.getElementById('ergebnis');
    const wettePlatzierenBtn = document.getElementById('wettePlatzieren');
    const spinRouletteBtn = document.getElementById('spinRouletteBtn');
    const rouletteWheel = document.getElementById('rouletteWheel');
    
    let currentPlayer = 'Spieler 1';
    let players = {
        'Spieler 1': 1000,
        'Spieler 2': 1000
    };
    
    function updatePlayerBalance() {
        document.getElementById('spieler1Geld').innerHTML = `<i class="