/**
 * Created by rico on 03/07/18.
 */

$body = $("body");

$(document).on({
    ajaxStart: function () {
        $body.addClass("loading");
    },
    ajaxStop: function () {
        $body.removeClass("loading");
    }
});

window.onload = function () {

    // create data object
    var data = new CAIXAGEST();

    function createElement(id, name) {
        $("<li><a href='#" + id + "'>" + name + "</a></li>").appendTo("#list");
        $("<div/>", {id: id}).appendTo("#tabs");
        $("<div/>", {
            id: "chartContainer-" + id,
            name: name,
            class: "chart",
            title: name.toUpperCase()
        }).appendTo("#" + id);
    }

    $.when(data.init()).then(function (funds) {
        $.when.apply(null, funds).done(function () {

            // create element
            createElement("all", "Dashboard");
            // build the chart
            new Chart("#chartContainer-all").init(data.funds);

            $.each(data.funds, function (index, fund) {
                createElement(fund.id, fund.name);
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
