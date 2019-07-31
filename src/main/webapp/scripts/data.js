/**
 * Created by rico on 03/07/18.
 */

function CAIXAGEST() {
    this.funds = [];
    this.stats = [];

    this.init = function () {
        var that = this;
        var deferreds = [];
        var dfd = $.Deferred();

        $.when($.getJSON("http://localhost:8080/caixagest/funds")).then(function (funds) {

            deferreds.push($.getJSON("http://localhost:8080/caixagest/funds/statistics").success(
                function (stats) {
                    $.each(stats, function (index, stat) {
                        that.stats[stat.id] = stat;
                    });
                })
            );

            $.each(funds, function (index, fund) {
                deferreds.push($.getJSON("http://localhost:8080/caixagest/funds/" + fund.id + "/rates").success(
                    function (rates) {
                        var highValue = 0, lowValue = 100, high = 0, low = 0;
                        $.each(rates, function (index, rate) {
                            if (rate.y > highValue) {
                                highValue = rate.y;
                                high = rate;
                            }
                            if (rate.y < lowValue) {
                                lowValue = rate.y;
                                low = rate;
                            }
                            rate.x = new Date(rate.x)
                        });
                        fund.low = low;
                        fund.high = high;
                        fund.rates = rates;

                        that.funds.push(fund);
                    })
                );
            });
            dfd.resolve(deferreds);
        });

        return dfd.promise();
    }
}
