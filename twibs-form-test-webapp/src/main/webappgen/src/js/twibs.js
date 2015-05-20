/*
 * Copyright (C) 2013-2014 by Michael Hombre Brinkmann
 */

Modernizr.load({
    test: Modernizr.input.placeholder,
    nope: [
        'https://cdnjs.cloudflare.com/ajax/libs/jquery-placeholder/2.0.8/jquery.placeholder.min.js'
    ]
});

var Twibs = {
    updateAfterDomChange: function () {
        $(document).trigger("twibs-update-dom");
    },
    pushState: function (state, title, pathname) {
        if (window.location.pathname != pathname) {
            window.history.replaceState("twibs-reload", document.title, document.location.href);
            window.history.pushState(state, title, pathname);
            document.title = title;
        }
    },
    mailme: function (host, name, subject) {
        if (subject) {
            self.location = "mail" + "to:" + name + "@" + host + "?subject=" + subject;
        } else {
            self.location = "mail" + "to:" + name + "@" + host;
        }
        return false;
    }
};

$(function () {
        var cp = typeof contextPath !== 'undefined' ? contextPath : '';

        $.fn.ckeditor = function (callback, config) {
            $('<script src="' + cp + '/ckeditor/ckeditor.js"></script><script src="' + cp + '/ckeditor/adapters/jquery.js"></script>').appendTo('head');
            $(this).ckeditor(callback, config);
        };

        $.fn.htmlAndUpdate = function (htmlString) {
            var ret = this.each(function () {
                $(this).html(htmlString);
            });
            Twibs.updateAfterDomChange();
            return ret;
        };

        $.fn.submitForm = function (reason, actionId, name, value) {
            var data = {};
            if (reason) data['form-action-reason'] = reason;
            if (actionId) data['form-action-id'] = actionId;
            if (name) {
                data['form-action-name'] = name;
                if (value !== undefined) data[name] = value;
            }
            var focusedId = $(document.activeElement).attr("id");
            if (focusedId) data['form-focused-id'] = focusedId;
            var bf = $("html").attr("class");
            if (bf) data["browser-features"] = bf;
            var config = {data: data};
            $(this).each(function () {
                ajaxSubmitForm($(this).closest('form'), config);
            });
            return this;
        };

        $.fn.twibsModal = function () {
            return this.each(function () {
                var $this = $(this);
                $this
                    .on('hidden.bs.modal', function () {
                        $this.remove();
                        $('body').updateDynamics();
                    })
                    .on('shown.bs.modal', function () {
                        $this.find('input:visible,.btn:visible').first().focus();
                        Twibs.updateAfterDomChange();
                    }).modal();
            })
        };

        $.fn.disableForm = function () {
            return this.each(function () {
                var $this = $(this);
                $this.find('.can-be-disabled').attr('disabled', 'disabled');
                $this.find('.can-be-disabled-by-class').addClass('disabled');
                $this.find('select.form-control').trigger("chosen:updated");
            });
        };

        $.fn.reenableForm = function () {
            return this.each(function () {
                var $this = $(this);
                $this.find('.can-be-disabled').removeAttr('disabled');
                $this.find('.can-be-disabled-by-class').removeClass('disabled');
            })
        };

        /* $x.ckeditorGet().destroy() does not work as it takes only the first ckeditor instance */
        $.fn.destroyCkeditor = function () {
            return this.each(function () {
                var ckeditor = $(this).data("ckeditorInstance");
                if (ckeditor) ckeditor.destroy();
            })
        };

        $.fn.closePopoversByScript = function () {
            return this.each(function () {
                var $this = $(this);
                //$this.find('[data-toggle=popover],.popover-by-script').popover('hide');
                //$this.find('.popover.in').detach();
            })
        };

        $.fn.updateDynamics = function () {
            return this.each(function () {
                var $this = $(this);
                $this.find(".twibs-form:visible").submitForm("update-dynamics", "");
                //$this.closePopoversByScript();
            })
        };

        var submitWhileTypingTimer;

        $(window)
            .on('hashchange', hashchange)
            .on('load', Twibs.updateAfterDomChange)
            .on('popstate', function (e) {
                if (e.originalEvent.state == "twibs-reload") {
                    location.reload()
                }
            })
            .scroll(positionFixedContainers);

        $(document)
            .on('click', '.twibs-form button[type="submit"],.twibs-form input[type="submit"]', function (e) {
                e.stopPropagation();
                e.preventDefault();
                var $this = $(this);
                $this.closest('form').submitForm("click", $this.id, $this.attr('name'), $this.val());
            })
            .on('click', '.submit', function (e) {
                var $this = $(e.target);
                if (!$this.closest(".ignore-submit,.submit").hasClass("ignore-submit")) {
                    e.stopPropagation();
                    e.preventDefault();
                    var $button = $this.closest(".submit");
                    $button.submitForm("click", $button.id, $button.attr('name'), $button.attr('value'));
                }
            })
            .on('keyup', '.submit-while-typing', function (e) {
                clearTimeout(submitWhileTypingTimer);
                var $this = $(e.target);
                submitWhileTypingTimer = setTimeout(function () {
                    $this.trigger("change")
                }, 300);
            })
            .on('change', '.submit-while-typing', function (e) {
                var $this = $(e.target);
                var was = $this.data("previous");
                var val = $this.val();
                if (was != val) {
                    $this.data("previous", val);
                    $this.closest('form').submitForm("submit-while-typing", $this.id, $this.data("name") || $this.attr('name'));
                }
            })
            .on('click', '.input-clear', function (e) {
                var $prev = $(e.target).prev();
                $prev.val("");
                $prev.focus();
                $prev.trigger("change");
            })
            .on('change', '.twibs-form .submit-on-change', function () {
                var $this = $(this);
                window.setTimeout(function () {
                    $this.submitForm("submit-on-change", $this.id, $this.data("name") || $this.attr('name'))
                }, 1);
            })
            .on('click', '[data-dismiss="detachable"]', function (e) {
                e.preventDefault();
                var $parent = $(this).closest(".detachable");
                $parent.fadeOut(200, function () {
                    $parent.detach();
                });
            })
            .on("click", function (e) {
                //var $target = $(e.target);
                //if ($target.closest('.popover').length == 0) {
                //    $('[data-toggle=popover],.popover-by-script').not($target.closest('[data-toggle=popover]')).popover('hide');
                //    // Bootstrap 3.0.2 does not detach the popup on hide (wait until fade out is done).
                //    setTimeout(function () {
                //        $('.popover').not(".in").detach()
                //    }, 400)
                //}
            })
            .on("twibs-update-dom", function () {
                $('.twibs-form select.chosen').chosen({disable_search_threshold: 6, width: '100%'});
                $('.twibs-form select.chosen-optional').chosen({
                    allow_single_deselect: true,
                    disable_search_threshold: 6,
                    width: '100%'
                });

                // 'chosen' does not preserve the focus, 'twibs' does.
                $('.twibs-form select.chosen:focus').each(function () {
                    $(this).parent().find(".chosen-container .chosen-drop .chosen-search input[type='text']").focus();
                });

                $('[data-toggle="popover"]').popover();

                $('[data-toggle="tooltip"]').tooltip();

                $('.twibs-form .sortable')
                    .sortable({forcePlaceholderSize: true})
                    .bind('sortupdate', function (e, ui) {
                        ui.item.submitForm("sortable", $ui.item.id);
                    });

                $('input.numeric').TouchSpin();

                $('textarea.hidden-print').each(function () {
                    var $this = $(this);
                    var $next = $this.next("div.textarea-print");
                    if ($next.length === 0) {
                        $next = $('<div class="textarea-print form-control visible-print"></div>').insertBefore($this);
                    }
                    $next.html($this.val());
                });

                initFixedContainers();
                positionFixedContainers();

                $('.click-on-appear').removeClass('.click-on-appear').filter("[data-call]").appear(function () {
                    $(this).trigger("click");
                });
            });

        $('body')
            .on("focus", ".show-focus-on-parent", function (e) {
                $(this).closest(".inherit-focus-from-child").addClass("focused");
            })
            .on("blur", ".show-focus-on-parent", function (e) {
                $(this).closest(".inherit-focus-from-child").removeClass("focused");
            })
            .on("click", '[data-call]', function (e) {
                e.preventDefault();
                var $this = $(this);
                $this.addClass("disabled");
                $.ajax({
                    url: $this.attr('data-call'),
                    dataType: "script"
                }).fail(failError).done(function () {
                    $this.removeClass("disabled");
                    Twibs.updateAfterDomChange();
                });
            });

        $('a[data-toggle="tab"]')
            .on('shown.bs.tab', function (e) {
                $($(e.target).attr('href')).updateDynamics();
            })
            .on('show.bs.tab', function (e) {
                return location.hash = $(e.target).attr('href').substr(1);
            });

        function hashchange() {
            $('a[href="' + location.hash + '"]').tab('show');
        }

        if (location.hash !== '') hashchange();

        function ajaxSubmitForm($form, config) {
            var $modal = $form.find(".transfer-modal");
            var $bar = $modal.find('.progress-bar');
            var $percent = $modal.find('.transfer-percent');
            var modalTimeout;
            var disableTimeout;

            function update(percent) {
                $bar.width(percent + '%').attr("aria-valuenow", percent);
                $percent.html(percent);
            }

            $form.ajaxSubmit($.extend(config, {
                dataType: 'script',
                error: function (jqxhr, status, error, $form) {
                    clearTimeout(modalTimeout);
                    clearTimeout(disableTimeout);
                    showAjaxError(jqxhr, error);
                    $form.reenableForm();
                },
                beforeSend: function () {
                    update(0);
                    modalTimeout = setTimeout(function () {
                        $modal.modal({backdrop: 'static', keyboard: false});
                    }, 2000);
                    disableTimeout = setTimeout(function () {
                        $form.disableForm();
                    }, 250);
                },
                dataFilter: function (d) {
                    update(100);
                    clearTimeout(modalTimeout);
                    clearTimeout(disableTimeout);
                    $modal.modal("hide");
                    return d;
                },
                uploadProgress: function (event, position, total, percentComplete) {
                    update(percentComplete);
                },
                success: function () {
                    Twibs.updateAfterDomChange();
                }
            }));
        }

        function failError(jqxhr, textStatus, exception) {
            showAjaxError(jqxhr, exception.message);
        }

        function showAjaxError(jqxhr, error) {
            var regex = /<div class="twibs-message-detail">(.*)<\/div>/g;
            var arr = regex.exec(jqxhr.responseText);
            var text = arr != null ? arr[0] : error;
            if (!text) {
                text = jqxhr.statusText;
                if (text == "error") {
                    if (jqxhr.status == 404)
                        text = "Not found";
                    else if (jqxhr.status == 0 && jqxhr.readyState === 0)
                        text = "Server unreachable";
                    else
                        text = "Unkown error";
                }
            }
            showError(text);
        }

        function showError(content) {
            new PNotify({
                text: content,
                type: 'error',
                nonblock: {
                    nonblock: true
                }
            });
        }

        function initFixedContainers() {
            $(".fixed-container").not(".fixed-managed").each(function () {
                var container = $(this);
                container.addClass("fixed-managed");
                var item = $(this).find(".fixed-content");
                var width = item.width();
                var height = item.height();

                container.css("width", width);
                container.css("height", height);
                item.css("position", "absolute").css("left", "auto").css("top", "auto").css("width", width).css("height", height);
            });
        }

        function positionFixedContainers() {
            var scrollTop = $(document).scrollTop();

            $(".fixed-container.fixed-managed").each(function () {
                var container = $(this);
                var item = $(this).find(".fixed-content");
                var width = container.css("width");
                var height = container.css("height");
                var offset = container.offset();
                var fixed = item.hasClass("fixed");
                if (offset.top < scrollTop) {
                    if (!fixed) {
                        item.css({position: "fixed", left: offset.left, top: 0});
                        item.addClass("fixed");
                    }
                } else if (fixed) {
                    item.css({position: "absolute", left: "auto", top: "auto"});
                    item.removeClass("fixed");
                }
            });
        }

        // Fix autofill in firefox: Reset disabled state after load
        // (firefox disables fields after ajax submit and page reload).
        // Either "disabled" or "can-be-disabled" can be given, not both.
        if (/firefox/i.test(navigator.userAgent)) {
            $(document).find('.can-be-disabled').removeAttr('disabled');
            $(document).find('.can-be-disabled-by-class').removeClass('disabled');
            // And restore the values from html. Do not use "autocomplete="off" in html to preserve autocomplete feature
            $('input').not('[autocomplete]').not('[type=checkbox]').not('[type=radio]').each(function () {
                $(this).val(this.getAttribute("value") || '');
            });
            $('textarea').not('[autocomplete]').each(function () {
                $(this).val(this.innerHTML || '');
            });
            $('input[type=checkbox]').not('[autocomplete]').each(function () {
                this.checked = this.hasAttribute("checked");
            });
            $("select").not('[autocomplete]').find("option").each(function () {
                this.selected = this.hasAttribute("selected");
            });
        }

        // Fire an event that twibs (and jquery) loading is complete
        var event = document.createEvent("Event");
        event.initEvent("twibs-loaded", true, true);
        document.dispatchEvent(event);
    }
);
