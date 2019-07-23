/**
 * Created by rico on 10/07/18.
 */

function Chart(element) {

    this.element = $(element);

    this.config = {
        animationEnabled: true,
        zoomEnabled: true,
        title: {
            text: "Rates"
        },
        axisX: {
            title: "Day",
            valueFormatString: "DD-MM-YY"
        },
        axisY: {
            interval: 3,
            gridColor: "lightgreen",
            title: "Value",
            includeZero: true,
            valueFormatString: "###.#####"
        },
        legend: {
            cursor: "pointer",
            fontSize: 11,
            itemclick: function (e) {
                e.dataSeries.visible = !(typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible);
                e.chart.render();
            }
        },
        toolTip: {
            shared: true
        },
        data: []
    };

    this.init = function (funds, markers) {
        this.element.CanvasJSChart(this.config);

        var that = this;
        $.each(funds, function (index, fund) {

            if (markers) {
                $.each(fund.rates, function (index, rate) {
                    if (fund.high.y === rate.y) {
                        rate.indexLabel = "high";
                        rate.markerType = "triangle";
                        rate.markerColor = "red";
                        rate.markerSize = 12;
                    } else if (fund.low.y === rate.y) {
                        rate.indexLabel = "low";
                        rate.markerType = "cross";
                        rate.markerColor = "red";
                        rate.markerSize = 12;
                    }
                });
            }

            that.config.data.push({
                name: fund.name,
                type: "spline",
                yValueFormatString: "###.#####",
                showInLegend: true,
                dataPoints: fund.rates
            });
        });
        this.element.CanvasJSChart().render();
    };

    this.setup = function (settings) {
        $.extend(this.config, settings);
    };
}