/**
 * Created by akazatchkov on 29/05/2014.
 */


(function(){
    AJS.bind("init.rte", function() {
        var pasteHandler = function(uri, node, done){

            var retrieveTitleViaBackend = function(pageUrl) {
                return AJS.$.ajax({
                    type: "get",
                    dataType: "json",
                    url: Confluence.getContextPath() + "/rest/sharelinks/1.0/link",
                    data: {
                        url: pageUrl
                    }
                });
            };

            if ((uri.protocol === "http" || uri.protocol === "https") && node.text() === uri.source) {
                retrieveTitleViaBackend(uri.source)
                    .done(function(linkData) {
                        if (linkData && linkData.title) {
                            node.text(linkData.title);
                            done(node);
                            return;
                        }
                        done();
                    })
                    .fail(function() {
                        done();
                    });
            } else {
                done();
            }
        };

        tinymce.plugins.Autoconvert.autoConvert.addHandler(pasteHandler, 5000);
    });

})();