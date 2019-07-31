/**
 * Created by rico on 03/07/18.
 */

$body = $("body");

/**
 * Added triggers to ajax requests to display a loader during requests and data process.
 */
$(document).on({
    ajaxStart: function () {
        $body.addClass("loading");
    },
    ajaxStop: function () {
        $body.removeClass("loading");
    }
});

/**
 * On load init function.
 */
window.onload = function () {

    // create data object
    var data = new CAIXAGEST();

    /**
     * Create the chart container for each fund.
     *
     * @param id the fund id
     * @param name the fund name
     */
    function createChartContainer(id, name) {
        $("<li><a href='#" + id + "'>" + name + "</a></li>").appendTo("#list");
        $("<div/>", {id: id}).appendTo("#tabs");
        $("<div/>", {
            id: "chartContainer-" + id,
            name: name,
            class: "chart",
            title: name.toUpperCase()
        }).appendTo("#" + id);
    }

    /**
     * Create the statistics container for each fund.
     *
     * @param id the fund id
     * @param name the fund name
     */
    function createStatsContainer(id, name) {
        $("<div/>", {
            id: "statistics-" + id,
            name: name,
            class: "statistics",
            title: name.toUpperCase()
        }).appendTo("#" + id);

        function createRow(id, label, value) {
            $("<div><span>"+label+":</span><span>"+value+"</span></div>").appendTo("#statistics-" + id);
        }

        createRow(id, "Today", data.stats[id].today);
        createRow(id, "Yesterday", data.stats[id].yesterday);
        createRow(id, "Day Before", data.stats[id].dayBeforeYesterday);
        createRow(id, "Max", data.stats[id].max);
        createRow(id, "Min", data.stats[id].min);
        createRow(id, "Average", data.stats[id].average);
        createRow(id, "Std. Deviation", data.stats[id].standardDeviation);
    }

    // Let's initialize all data performing requests to backend
    $.when(data.init()).then(function (allData) {
        // afterwards let's do some stuff with it
        $.when.apply($, allData).done(function () {
            // create chart element
            createChartContainer("all", "Dashboard");
            // build the global chart with all funds data
            new Chart("#chartContainer-all").init(data.funds);
            //individually create a chart and statistics for each fund
            $.each(data.funds, function (index, fund) {
                // create the chart container element for the fund
                createChartContainer(fund.id, fund.name);
                // create the statistic container element for the fund
                createStatsContainer(fund.id, fund.name);
                // initialize the chart for the fund
                var chart = new Chart("#chartContainer-" + fund.id);
                chart.setup({
                    title: {
                        text: fund.name
                    }
                });
                chart.init([fund], true);
            });

            // create the tab
            $("#tabs").tabs();
        });
    });
};
