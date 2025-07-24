$(document).ready(function() {

    // 중앙에 텍스트를 표시하는 플러그인
    const centerTextPlugin = {
        id: 'centerText',
        beforeDraw: function(chart) {
            let width = chart.width,
                height = chart.height,
                ctx = chart.ctx;

            // 상태 복원
            ctx.restore();
            let fontSize = (height / 114).toFixed(2);
            ctx.font = fontSize + "em sans-serif";
            ctx.fillStyle = 'rgba(255, 99, 132, 0.5)';
            ctx.textBaseline = "middle";

  			let text = chart.data.datasets[0].data[0] + "%",
            textX = Math.round((width - ctx.measureText(text).width) / 2),
            textY = (height / 2) + 100;

            ctx.fillText(text, textX, textY);

            // 상태 저장
            ctx.save();
        }
    };

    // 플러그인 등록
    Chart.register(centerTextPlugin);

    // CPU 및 메모리 데이터 설정
    let cpuData = {
        datasets: [{
            data: [0, 100],
            backgroundColor: ['rgba(255, 99, 132, 0.5)', 'rgba(54, 162, 235, 0.5)'],
            borderWidth: 1
        }],
        labels: ['사용량', '여유량']
    };

    let memoryData = {
        datasets: [{
            data: [0, 100],
            backgroundColor: ['rgba(75, 192, 192, 0.5)', 'rgba(153, 102, 255, 0.5)'],
            borderWidth: 1
        }],
        labels: ['사용량', '여유량']
    };

    let options = {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '60%',
        rotation: -90,
        circumference: 180,
        animation: { duration: 1000 },
        plugins: { legend: {display: true} }
    };

    // 차트 생성
    let cpuChart = new Chart(document.getElementById('cpuChart'), {
        type: 'doughnut',
        data: cpuData,
        options: options,
        plugins: [centerTextPlugin]
    });

    let memoryChart = new Chart(document.getElementById('memoryChart'), {
        type: 'doughnut',
        data: memoryData,
        options: options,
        plugins: [centerTextPlugin]
    });

    // 3초마다 데이터 업데이트
    setInterval(function () {
        fetch('/system/cpuData')
            .then(response => response.json())
            .then(data => {
                cpuChart.data.datasets[0].data[0] = Number(data.checkData);
                cpuChart.data.datasets[0].data[1] = 100 - Number(data.checkData);
                
                memoryChart.data.datasets[0].data[0] = Number(data.memoryFree);
                memoryChart.data.datasets[0].data[1] = 100 - Number(data.memoryFree);
                
                cpuChart.update();
                memoryChart.update();
            });
    }, 3000);
});