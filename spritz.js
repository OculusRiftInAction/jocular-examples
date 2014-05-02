if ("undefined" == typeof jQuery) throw new Error("Spritz's JavaScript requires jQuery");
if (function (a) {
    var b = a("#spritzjs");
    if (0 === b.length) window.spritz_sdk_root = "//sdk.spritzinc.com/js/1.0";
    else {
        var c = b.attr("src"),
            d = c.split("/"),
            e = "";
        if (d.length < 3) e = ".";
        else for (var f = 0; f < d.length - 2; f++) e += d[f], e += "/";
        window.spritz_sdk_root = e
    }
}(jQuery), !document.getElementById("spritz-css")) {
    var head = document.getElementsByTagName("head")[0],
        link = document.createElement("link");
    link.id = "spritz-css", link.rel = "stylesheet", link.type = "text/css", link.href = window.spritz_sdk_root + "/css/spritz.min.css", link.media = "all", head.appendChild(link)
}
var SPRITZ = SPRITZ || {};
SPRITZ.utils = {}, SPRITZ.utils.debugLevel = 0, SPRITZ.utils.setDebugLevel = function (a) {
    SPRITZ.utils.debugLevel = a
}, SPRITZ.namespace = function (a) {
    var b, c = a.split("."),
        d = SPRITZ;
    for ("SPRITZ" === c[0] && (c = c.slice(1)), b = 0; b < c.length; b += 1)"undefined" == typeof d[c[b]] && (d[c[b]] = {}), d = d[c[b]];
    return d
}, SPRITZ.addToNamespace = function (a, b) {
    var c = b();
    for (var d in c) c.hasOwnProperty(d) && (a[d] = c[d])
}, SPRITZ.utils.supportsCanvas = function () {
    return !!document.createElement("canvas").getContext()
}, SPRITZ.utils.supportsCanvasText = function () {
    if (!SPRITZ.utils.supportsCanvas()) return !1;
    var a = document.createElement("canvas"),
        b = a.getContext("2d");
    return "function" == typeof b.fillText
}, SPRITZ.utils.supportsHtmlStorage = function () {
    try {
        return "localStorage" in window && null !== window.localStorage
    } catch (a) {
        return !1
    }
}, SPRITZ.utils.endsWith = function (a, b) {
    return -1 !== a.indexOf(b, a.length - b.length)
}, SPRITZ.utils.timestampToString = function (a, b) {
    "undefined" == typeof b && (b = new Date);
    var c = "",
        d = Math.round((b.getTime() - a) / 1e3);
    if (60 > d) c = d + "s ago";
    else {
        var e = Math.round((b.getTime() - a) / 6e4);
        if (60 > e) c = e + "m ago";
        else {
            var f, g, h = new Date(a);
            h.getHours() > 12 ? (f = h.getHours() - 12, g = "PM") : (f = h.getHours(), g = "AM");
            var i = f + ":";
            h.getMinutes() < 10 && (i += "0"), i = i + h.getMinutes() + " " + g, c = h.getDate() === b.getDate() && h.getMonth() === b.getMonth() && h.getFullYear() === b.getFullYear() ? i : h.getMonth() + 1 + "/" + h.getDate() + "/" + h.getFullYear().toString().substring(2, 4) + " " + i
        }
    }
    return c
};
var jsutil = jsutil ||
function () {
    var a = null,
        b = 200,
        c = 220,
        d = function (a, b) {
            if ("function" != typeof b) throw new Error("method parameter is not a function [" + typeof b + "]");
            var c = function () {
                    return b.apply(a, arguments)
                };
            return c
        },
        e = function (d, e) {
            if ("undefined" == typeof e && (e = d, d = 0), console && d <= SPRITZ.utils.debugLevel && console.log(e), null === a && (a = jQuery("#spritzDebugPage .ui-page-content"), 0 === a.length && (a = null)), a && d <= SPRITZ.utils.debugLevel) {
                var f;
                switch (d) {
                case 1:
                    f = "dbg-err";
                    break;
                case 2:
                    f = "dbg-warn";
                    break;
                case 3:
                    f = "dbg-3";
                    break;
                case 4:
                    f = "dbg-4";
                    break;
                case 5:
                    f = "dbg-5";
                    break;
                default:
                    f = "dbg-info"
                }
                jQuery('<div class="' + f + '">' + e + "</div>").appendTo(a);
                var g = a.children("div");
                if (g.length > c) {
                    for (var h = g.length - b, i = h - 1; i >= 0; i--) g[i].remove();
                    jQuery('<div class="dbg-info;">' + (new Date).toString() + " - Removed " + h + " entries</div>").appendTo(a)
                }
            }
        };
    return {
        createDelegate: d,
        debug: e
    }
}(); +
function (a) {
    function b(b) {
        a(d).remove(), a(e).each(function () {
            var d = c(a(this)),
                e = {
                    relatedTarget: this
                };
            d.hasClass("open") && (d.trigger(b = a.Event("hide.bs.dropdown", e)), b.isDefaultPrevented() || d.removeClass("open").trigger("hidden.bs.dropdown", e))
        })
    }
    function c(b) {
        var c = b.attr("data-target");
        c || (c = b.attr("href"), c = c && /#[A-Za-z]/.test(c) && c.replace(/.*(?=#[^\s]*$)/, ""));
        var d = c && a(c);
        return d && d.length ? d : b.parent()
    }
    var d = ".dropdown-backdrop",
        e = "[data-toggle=dropdown]",
        f = function (b) {
            a(b).on("click.bs.dropdown", this.toggle)
        };
    f.prototype.toggle = function (d) {
        var e = a(this);
        if (!e.is(".disabled, :disabled")) {
            var f = c(e),
                g = f.hasClass("open");
            if (b(), !g) {
                "ontouchstart" in document.documentElement && !f.closest(".navbar-nav").length && a('<div class="dropdown-backdrop"/>').insertAfter(a(this)).on("click", b);
                var h = {
                    relatedTarget: this
                };
                if (f.trigger(d = a.Event("show.bs.dropdown", h)), d.isDefaultPrevented()) return;
                f.toggleClass("open").trigger("shown.bs.dropdown", h), e.focus()
            }
            return !1
        }
    }, f.prototype.keydown = function (b) {
        if (/(38|40|27)/.test(b.keyCode)) {
            var d = a(this);
            if (b.preventDefault(), b.stopPropagation(), !d.is(".disabled, :disabled")) {
                var f = c(d),
                    g = f.hasClass("open");
                if (!g || g && 27 == b.keyCode) return 27 == b.which && f.find(e).focus(), d.click();
                var h = " li:not(.divider):visible a",
                    i = f.find("[role=menu]" + h + ", [role=listbox]" + h);
                if (i.length) {
                    var j = i.index(i.filter(":focus"));
                    38 == b.keyCode && j > 0 && j--, 40 == b.keyCode && j < i.length - 1 && j++, ~j || (j = 0), i.eq(j).focus()
                }
            }
        }
    };
    var g = a.fn.dropdown;
    a.fn.dropdown = function (b) {
        return this.each(function () {
            var c = a(this),
                d = c.data("bs.dropdown");
            d || c.data("bs.dropdown", d = new f(this)), "string" == typeof b && d[b].call(c)
        })
    }, a.fn.dropdown.Constructor = f, a.fn.dropdown.noConflict = function () {
        return a.fn.dropdown = g, this
    }, a(document).on("click.bs.dropdown.data-api", b).on("click.bs.dropdown.data-api", ".dropdown form", function (a) {
        a.stopPropagation()
    }).on("click.bs.dropdown.data-api", e, f.prototype.toggle).on("keydown.bs.dropdown.data-api", e + ", [role=menu], [role=listbox]", f.prototype.keydown)
}(jQuery), SPRITZ.namespace("SPRITZ.component"), SPRITZ.addToNamespace(SPRITZ.component, function () {
    function a(a) {
        this.container = a instanceof jQuery ? a : b(a), a.data("controller", this), this.button = b("button", this.container), this.currentValue = null, c.call(this)
    }
    var b = jQuery,
        c = function () {
            b(".dropdown-menu", this.container).on("click", "li", jsutil.createDelegate(this, d));
            var a = b("li.selected", this.container);
            0 == a.length && (a = b(".dropdown-menu li:first-child", this.container)), this.currentValue = a.data("value")
        },
        d = function (a) {
            var c = b(a.currentTarget);
            f.call(this, c), e.call(this, c.data("value"))
        },
        e = function (a, b) {
            if (this.currentValue == a);
            else {
                var c = this.currentValue;
                this.currentValue = a, b || this.container.trigger("change", {
                    oldValue: c,
                    newValue: a
                })
            }
        },
        f = function (a) {
            var c = b("a", a);
            this.button.html(c.html() + ' <span class="caret"></span>'), b(".dropdown-menu li", this.container).removeClass("selected"), a.addClass("selected")
        };
    return a.prototype.setValue = function (a, c) {
        var d = b("li[data-value='" + a + "']", this.container);
        return 1 == d.length && (f.call(this, d), e.call(this, d.data("value"), c)), 1 == d.length
    }, b(document).ready(function () {
        b(".btn-group[data-role='select']").each(function () {
            var a = b(this);
            a.data("controller") instanceof BootstrapSelectContainer || new SPRITZ.component.BootstrapSelectController(a)
        })
    }), {
        BootstrapSelectController: a
    }
}), SPRITZ.namespace("SPRITZ.model"), SPRITZ.model.TimedWord = function (a, b, c, d, e) {
    var f = SPRITZ.model;
    f.TimedWord.FLAG_MASK = 7, f.TimedWord.FLAG_SENTENCE_START = 1, f.TimedWord.FLAG_BOLD = 2, f.TimedWord.FLAG_PARAGRAPH_START = 4, this.word = a, this.orp = b, this.multiplier = c, this.position = d, this.flags = e, this.isBold = function () {
        return 0 != (this.flags & f.TimedWord.FLAG_BOLD)
    }, this.isSentenceStart = function () {
        return 0 != (this.flags & f.TimedWord.FLAG_SENTENCE_START)
    }, this.isParagraphStart = function () {
        return 0 != (this.flags & f.TimedWord.FLAG_PARAGRAPH_START)
    }, this.toString = function () {
        return '{"' + this.word + '", ' + this.orp + ", " + this.multiplier + ", " + this.position + (this.isParagraphStart() ? ", paragraphStart" : "") + (this.isSentenceStart() ? ", sentenceStart" : "") + (this.isBold() ? ", bold" : "") + "}"
    }
}, SPRITZ.model.SpritzText = function (a, b, c, d) {
    var e = 1.21,
        f = a,
        g = b,
        h = c,
        i = d,
        j = 0,
        k = null;
    this.getWords = function () {
        return f
    }, this.getLocale = function () {
        return h
    }, this.getVersion = function () {
        return i
    }, this.getWord = function (a) {
        return f[a]
    }, this.getCurrentWord = function () {
        return f[j]
    }, this.getNextWord = function () {
        return j < f.length ? (this.setCurrentIndex(++j), f[j]) : null
    }, this.hasNextWord = function () {
        return j < f.length
    }, this.getCurrentIndex = function () {
        return j
    }, this.setCurrentIndex = function (a) {
        j = a, null != k && k(j, f.length)
    }, this.size = function () {
        return f.length
    }, this.reset = function () {
        this.setCurrentIndex(0)
    }, this.getPreviousSentenceStart = function (a, b) {
        var c = SPRITZ.model;
        if (0 > a || a >= f.length) throw new c.ArrayIndexOutOfBoundsException(a);
        var d = a;
        if (b > 0) for (; d > 0 && b > 0 && (!f[d].isSentenceStart() || 0 != --b); d--);
        return d
    }, this.getNextSentenceStart = function (a, b) {
        var c = SPRITZ.model;
        if (0 > a || a >= f.length) throw new c.ArrayIndexOutOfBoundsException(a);
        var d = a;
        if (b > 0) for (; d < f.length && (!f[d].isSentenceStart() || 0 != --b); d++);
        return d
    }, this.calculateTime = function (a, b) {
        return Math.round(6e4 * g * (f.length - b) / (a * e * f.length))
    }, this.getTotalTime = function (a) {
        return this.calculateTime(a, 0)
    }, this.getRemainingTime = function (a) {
        return this.calculateTime(a, j)
    }, this.getProgressTracker = function () {
        return k
    }, this.setProgressTracker = function (a) {
        k = a
    }
}, SPRITZ.model.SpritzText.create = function (a, b, c, d) {
    var e = SPRITZ.model,
        f = "V2",
        g = "AAAC",
        h = goog.math.Long.fromInt(1073741823),
        i = goog.math.Long.fromInt(15),
        j = goog.math.Long.fromInt(16383),
        k = goog.math.Long.fromInt(7),
        l = 1,
        m = 2,
        n = 4;
    if (a !== f) throw new Error("Unknown container format");
    if (!("object" == typeof b && b instanceof Array && "object" == typeof c && c instanceof Array)) throw new Error("Invalid data format: wrong types");
    if (0 == c.length) throw new Error("Invalid data format: data2");
    var o = c[0];
    if ("string" != typeof o) throw new e.IllegalArgumentException("Invalid preamble");
    var p = o.split(",");
    if (p.length < 4) throw new Error("Invalid preamble");
    if (p[0] != g) throw new Error("Unrecognized encoding");
    var q = parseInt(p[1]);
    if (b.length != q || c.length - 1 != q) throw new Error("Invalid data format: Wrong data length");
    for (var r = [], s = 0; q > s; s++) {
        var t = b[s],
            u = c[s + 1],
            v = goog.math.Long.fromString(u, 16),
            w = v.and(k).toInt(),
            x = 0;
        0 != (w & l) && (x |= e.TimedWord.FLAG_BOLD), 0 != (w & m) && (x |= e.TimedWord.FLAG_SENTENCE_START), 0 != (w & n) && (x |= e.TimedWord.FLAG_PARAGRAPH_START), v = v.shiftRight(3);
        var y = v.and(j).getLowBits();
        v = v.shiftRight(14);
        var z = v.and(i).getLowBits();
        v = v.shiftRight(4);
        var A = v.and(h).getLowBits();
        r.push(new e.TimedWord(t, z, y, A, x))
    }
    return new SPRITZ.model.SpritzText(r, d, p[2], p[3])
}, SPRITZ.model.IllegalArgumentException = function (a) {
    this.name = "IllegalArgumentException", this.message = a
}, SPRITZ.model.IllegalArgumentException.prototype = new Error, SPRITZ.model.IllegalArgumentException.prototype.constructor = SPRITZ.model.IllegalArgumentException, SPRITZ.model.ArrayIndexOutOfBoundsException = function (a) {
    this.name = "ArrayIndexOutOfBoundsException", this.message = a
}, SPRITZ.model.ArrayIndexOutOfBoundsException.prototype = new Error, SPRITZ.model.ArrayIndexOutOfBoundsException.prototype.constructor = SPRITZ.model.ArrayIndexOutOfBoundsException, SPRITZ.namespace("SPRITZ.display"), SPRITZ.display.SpritzReadingPanel = function (a) {
    var b = jQuery,
        c = "SpritzReadingPanel: ";
    jsutil.debug(3, c + "Created " + c);
    var d = Object.freeze({
        ready: 0,
        running: 1,
        paused: 2,
        completed: 3
    }),
        e = function () {
            cb = O + "px " + j, "undefined" != typeof localStorage && "ru" == localStorage.getItem("spritz.language") && (cb = O + "px " + k), db = "bold " + cb, eb = cb, fb = db
        },
        f = function () {
            M = a.width, N = a.height, O = Math.round(a.height * l);
            var b; - 1 == navigator.userAgent.toLowerCase().indexOf("firefox") ? (ab = 1, bb = 3, b = -1) : (ab = 2, bb = 2, b = 1), bb = bb * a.height / 60, P = Math.round(.56 * O + .5), Q = Math.round(.44 * O + .5), R = P + Q, S = Math.round(a.height * n), T = Math.round(v + (a.width - v - w) * m), U = Math.round(x + (a.height - x - y) / 2), V = U - Math.round((P + Q) / 2) + b, W = 0, X = a.width, Y = Math.round(a.width * p), Z = Math.round(a.height * o), $ = T - v, _ = a.width - w - T, e(), gb = a.getContext("2d"), gb.textBaseline = "top", gb.textAlign = "left";
            var d = 3;
            jsutil.debug(d, c + "dimensions: " + a.width + "x" + a.height), jsutil.debug(d, c + "fontSizeHeight: " + O), jsutil.debug(d, c + "ascent: " + P), jsutil.debug(d, c + "descent: " + Q), jsutil.debug(d, c + "focus: " + T + "," + U), jsutil.debug(d, c + "textY: " + V), jsutil.debug(d, c + "fontRealHeight: " + R), jsutil.debug(d, c + "textOverflow, top: " + ab + ", bottom: " + bb), jsutil.debug(d, c + "textNormalPaint: " + cb), jsutil.debug(d, c + "textNormalBoldPaint: " + db)
        },
        g = 200,
        h = 1.21,
        i = 6e4,
        j = "SpritzMedienMedium",
        k = "arial",
        l = .4,
        m = .35,
        n = .1,
        o = .113,
        p = .03,
        q = "#FFFFFF",
        r = "#000000",
        s = "#CC0001",
        t = "Black",
        u = 2,
        v = 0,
        w = 0,
        x = 0,
        y = 0,
        z = 750,
        A = z,
        B = "#E8E8E8",
        C = 5,
        D = null,
        E = g,
        F = null,
        G = null,
        H = 0,
        I = !0,
        J = d.ready,
        K = r,
        L = B,
        M = 0,
        N = 0,
        O = 0,
        P = 0,
        Q = 0,
        R = 0,
        S = 0,
        T = 0,
        U = 0,
        V = 0,
        W = 0,
        X = 0,
        Y = 0,
        Z = 0,
        $ = 0,
        _ = 0,
        ab = 0,
        bb = 0,
        cb = "",
        db = "",
        eb = "",
        fb = "",
        gb = null;
    f(), gb.fillStyle = q, gb.font = cb, gb.fillText(" ", 0, 0), gb.fillRect(0, 0, a.width, a.height);
    var hb = function (a, b, c, d, e, f) {
            gb.lineWidth = e, gb.strokeStyle = f, gb.beginPath(), gb.moveTo(a, b), gb.lineTo(c, d), gb.stroke()
        },
        ib = function () {
            var b = v + Y,
                c = a.width - w - Y,
                d = x + Z,
                e = a.height - y - Z;
            hb(b, d, c, d, u, t), hb(T, d, T, d + S, u, t), hb(b, e, c, e, u, t), hb(T, e - S, T, e, u, t)
        },
        jb = function (a, b) {
            gb.font = b;
            var c = gb.measureText(a);
            return c.width
        },
        kb = function (a, b, c, d, e) {
            gb.font = b, gb.fillStyle = c, gb.fillText(a, d, e)
        },
        lb = function (a, b, c, d, e) {
            gb.fillStyle = e, gb.fillRect(a, b, c, d)
        },
        mb = function () {
            lb(v, x, a.width - w, a.height - y, q)
        },
        nb = function () {
            K = I ? s : r
        },
        ob = function () {
            return D
        },
        pb = function () {
            var a = D.getCurrentIndex();
            if (a >= 0) {
                var b;
                D.hasNextWord() ? b = 2 : (a--, b = 1), a = D.getPreviousSentenceStart(a, b), "" == D.getWord(a).word && a < D.size() - 1 && (a += 1)
            }
            return a
        },
        qb = function () {
            var a = D.getCurrentIndex();
            return a < D.size() - 1 && (a = D.getNextSentenceStart(a + 1, 1)), a
        },
        rb = function () {
            if (J == d.running) {
                zb();
                var a = ob().getNextWord();
                if (null == a) {
                    if (J = d.completed, null != F) {
                        var b = F;
                        delete F, b()
                    }
                } else yb(a.word, a.orp, a.position, a.isBold()), G = setTimeout(function () {
                    G = null, rb()
                }, wb(a.multiplier))
            } else jsutil.debug(4, c + "displayNextWord interrupted on word # " + ob().getCurrentIndex() + " because of state: " + J)
        },
        sb = function () {
            H = (new Date).getTime(), tb(this)
        },
        tb = function () {
            var b = (new Date).getTime() - H,
                c = b / A,
                d = Math.max($ * c, 0),
                e = Math.max(_ * c, 0),
                f = a.height - x - y,
                g = a.width - v - w;
            mb(), lb(v + d, x, g - d - e, f, L), ib(), xb(), T >= d || e <= a.width - T ? G = setTimeout(function () {
                G = null, tb()
            }, C) : (mb(), ib(), xb(), rb(this))
        },
        ub = function () {
            return E
        },
        vb = function (a) {
            return Math.floor(i / (a * h))
        },
        wb = function (a) {
            return (1 + a / 100) * vb(ub())
        },
        xb = function () {
            var a = D.getCurrentWord();
            yb(a.word, a.orp, a.position, a.isBold())
        },
        yb = function (a, b, c, d) {
            if (null != a && 0 != a.length) {
                var e, f, g = V;
                d ? (e = db, f = fb) : (e = cb, f = eb);
                var h, i, j, k, l, m, n = a.length;
                0 == b ? (h = null, k = 0) : (h = a.substring(0, b), k = Math.round(jb(h, e))), i = a.substring(b, b + 1), l = Math.round(jb(i, f)), m = Math.round(l / 2), j = b == n - 1 ? null : a.substring(b + 1), k > 0 && kb(h, e, r, T - k - m, g), kb(i, f, K, T - m, g), null != j && kb(j, e, r, T + m, g)
            }
        },
        zb = function () {
            gb.fillStyle = q, gb.fillRect(W, V - ab, X - W, R + bb + 1)
        },
        Ab = function () {
            var d = b(a),
                e = d.width(),
                g = d.height();
            e == M && g == N ? jsutil.debug(3, c + "dimensions unchanged: " + M + "x" + N) : (jsutil.debug(3, c + "dimensions changed: " + M + "x" + N + " -> " + e + "x" + g), a.width = e, a.height = g, f())
        };
    ib(), this.getCurrentText = function () {
        return ob()
    }, this.getCurrentTextSpeed = function () {
        return ub()
    }, this.setCurrentTextSpeed = function (a) {
        E = a
    }, this.getCurrentPosition = function () {
        return null != D ? D.getCurrentIndex() : -1
    }, this.getHighlightBestLetter = function () {
        return I
    }, this.setHighlightBestLetter = function (a) {
        I = a, nb()
    }, this.isPaused = function () {
        return J == d.paused
    }, this.isReady = function () {
        return J == d.ready
    }, this.displayText = function (a, b) {
        if (jsutil.debug(4, c + "Displaying Text with " + a.size() + " words in " + E + " words/min"), D = a, F = b, D.hasNextWord()) J = d.running, e(), Ab(), nb(), sb();
        else if (null != F) {
            var b = F;
            delete F, b()
        }
    }, this.pauseText = function () {
        J == d.running ? (D ? jsutil.debug(4, c + "Pausing text with " + D.size() + " words at word # " + (D.getCurrentIndex() + 1)) : jsutil.debug(4, c + "No text to pause"), null != G && (clearTimeout(G), G = null), D.getCurrentIndex() > 0 && D.setCurrentIndex(D.getCurrentIndex() - 1), J = d.paused) : J = d.paused
    }, this.resumeText = function () {
        D ? D.hasNextWord() ? (jsutil.debug(4, c + "Resuming Text with " + D.size() + " words at word # " + (D.getCurrentIndex() + 1)), J = d.running, nb(), sb()) : jsutil.debug(4, c + "Cannot resume Text because there are no more words") : jsutil.debug(2, c + "Cannot resume Text because it is null")
    }, this.resetText = function () {
        this.pauseText(), D = null, zb()
    }, this.goToFirstWord = function () {
        D ? (D.reset(), mb.call(this), ib.call(this), xb()) : jsutil.debug(4, c + "Cannot go to the first word because the text is null")
    }, this.goToPreviousSentence = function () {
        D ? (D.setCurrentIndex(pb.call(this)), jsutil.debug(4, c + "Positioning Text with " + D.size() + " words at word # " + D.getCurrentIndex()), mb.call(this), ib.call(this), xb()) : jsutil.debug(4, c + "Cannot go to previous sentence because the text is null")
    }, this.goToNextSentence = function () {
        D ? (D.setCurrentIndex(qb.call(this)), jsutil.debug(4, c + "Positioning Text with " + D.size() + " words at word # " + D.getCurrentIndex()), mb.call(this), ib.call(this), D.hasNextWord() && xb()) : jsutil.debug(4, c + "Cannot go to previous sentence because the text is null")
    }, this.displayWord = function (a, b, c) {
        yb(a, b, 0, c)
    }, this.eraseWord = function () {
        zb()
    }
}, SPRITZ.namespace("SPRITZ.client"), SPRITZ.addToNamespace(SPRITZ.client, function () {
    var a = {
        name: "Spritz_JSSDK",
        version: "1.0.3",
        buildDate: "Tue Apr 01 2014 16:48:29 GMT-0600 (MDT)"
    };
    return {
        VersionInfo: a
    }
}), SPRITZ.addToNamespace(SPRITZ.namespace("SPRITZ.client"), function () {
    function a(a, b) {
        this.code = a, this.message = b
    }
    function b(a, b, c) {
        this.url = b + "/oauth/authorize?c=" + encodeURIComponent(SPRITZ.client.VersionInfo.name + "_" + SPRITZ.client.VersionInfo.version) + "&response_type=token&client_id=" + a + "&redirect_uri=" + encodeURI(c), this.onSuccess = null, this.onError = null
    }
    var c = function (a) {
            "function" == typeof window.removeEventListener ? (window.removeEventListener("message", this.onMessageHandler, !1), "object" == typeof console && console.log("Registered listener via addEventListener")) : window.detachEvent("onmessage", this.onMessageHandler), localStorage.removeItem("spritz.authResponse");
            var b = d(a.data);
            b.access_token && b.token_type ? "bearer" === b.token_type ? this.onSuccess && this.onSuccess(b.access_token) : this.onError && this.onError(new Error("Unexpected token type: " + b.token_type)) : this.onError && this.onError(new Error("No token data in response")), this.showing = !1, a.source.close()
        },
        d = function (a) {
            var b = {};
            if (null === a || 0 === a.length) return b;
            var c = a.indexOf("#");
            c >= 0 && (a = a.substring(c + 1));
            for (var d = a.split("&"), e = 0; e < d.length; e++) {
                var f, g, h = d[e],
                    i = h.indexOf("="); - 1 === i ? (f = decodeURIComponent(h), g = !0) : (f = decodeURIComponent(h.substring(0, i)), g = decodeURIComponent(h.substring(i + 1))), b[f] = g
            }
            return b
        };
    return b.prototype.show = function (a, b) {
        this.onSuccess = a, this.onError = b, "undefined" == typeof localStorage && localStorage.removeItem("spritz.authResponse");
        var d = window.open(this.url, "SpritzLogin", "width=400,height=400,location=0,menubar=0,toolbar=0");
        d || console && console.log("Unable to open popup"), d.focus(), this.showing = !0, this.onMessageHandler = jsutil.createDelegate(this, c), "function" == typeof window.addEventListener ? (window.addEventListener("message", this.onMessageHandler, !1), "object" == typeof console && console.log("Registered listener via addEventListener")) : window.attachEvent("onmessage", this.onMessageHandler);
        var e = this,
            f = null;
        f = setInterval(function () {
            if ("undefined" != typeof localStorage) {
                var a = localStorage.getItem("spritz.authResponse");
                null !== a && e.onMessageHandler({
                    data: a,
                    source: d
                })
            }
            e.showing && d ? d.closed && (clearInterval(f), e.showing = !1, e.onError && e.onError(new Error("Login aborted"))) : clearInterval(f)
        }, 250)
    }, {
        APIError: a,
        AuthPopup: b
    }
}), SPRITZ.addToNamespace(SPRITZ.namespace("SPRITZ.client"), function () {
    var a = jQuery,
        b = function (a, b) {
            return "spritz." + b + "Token." + a
        },
        c = function () {
            var a = this.apiRoot.indexOf("//");
            if (-1 === a) this.apiHost = "api.spritzinc.com";
            else {
                var b = this.apiRoot.indexOf("/", a + 2);
                this.apiHost = -1 === b ? this.apiRoot.substring(a + 2) : this.apiRoot.substring(a + 2, b)
            }
        },
        d = function (b, c) {
            var d = this,
                e = function (a) {
                    "access_token" in a && "token_type" in a && "bearer" === a.token_type ? "function" == typeof b && l.call(d, a.access_token, b, c) : "function" == typeof c && c(new Error("Client login failed, invalid response"))
                },
                g = function (a) {
                    var b = f.call(d, a);
                    if (403 === b.code && ("object" == typeof console && console.log("Client ID " + d.clientId + " is not authorized to use anonymous mode"), d.anonymousEnabled = !1), "function" == typeof c) {
                        var e = new Error("Unable to perform client login: " + b.message);
                        e.cause = b, c(e)
                    }
                };
            a.ajax({
                type: "GET",
                url: this.apiRoot + "/oauth/clientToken?clientId=" + encodeURIComponent(this.clientId),
                dataType: "json",
                async: !0,
                success: e,
                error: g
            })
        },
        e = function () {
            "/" === this.apiRoot.substring(this.apiRoot.length - 1) && (this.apiRoot = this.apiRoot.substring(0, this.apiRoot.length - 1))
        },
        f = function (a) {
            var b = null;
            return 500 === a.status || 403 === a.status, null === b && (b = new SPRITZ.client.APIError(a.status, "HTTP call failed, status: " + a.status + ", message: " + a.statusText)), b
        },
        g = function (b, c, e, g) {
            var i, j, l, m = 1,
                n = this;
            if (i = function () {
                console && console.log("Invoking " + b.type + " " + b.url);
                var c = function (a) {
                        if (401 === a.status && 1 === m) m++, "user" === l ? s.call(n, null) : "client" === l && r.call(n, null), j();
                        else {
                            var b = f(a);
                            g ? g(b) : console && console.log(b.code + ": " + b.message)
                        }
                    };
                "user" === l ? b.headers = {
                    Authorization: "Bearer " + n.userAccessToken
                } : "client" === l && (b.headers = {
                    Authorization: "Bearer " + n.clientAccessToken
                }), b.success = e, b.error = c, a.ajax(b)
            }, j = function () {
                if (c || null !== n.userAccessToken) if (l = "user", c && null === n.userAccessToken) {
                    var a = function (a) {
                            g && g(new Error("API call failed, login failed: " + a.message))
                        };
                    h.call(n, i, a)
                } else i();
                else if (l = "client", null === n.clientAccessToken) {
                    var b = function (a) {
                            "cause" in a && a.cause instanceof SPRITZ.client.APIError && 403 === a.cause.code ? ("object" == typeof console && console.log("Client login failed with status 403, retrying API call with userRequired"), c = !0, j()) : g && g(new Error("API call failed, client login failed: " + a.message))
                        };
                    d.call(n, i, b)
                } else i()
            }, n.initialized) j();
            else {
                var o = function (a) {
                        g && g(new Error("Execution failed, unable to get accessToken: " + a.message))
                    };
                k.call(n, j, o)
            }
        },
        h = function (a, b) {
            var c = new SPRITZ.client.AuthPopup(this.clientId, this.apiRoot, this.redirectUri),
                d = this,
                e = function (c) {
                    m.call(d, c, a, b)
                },
                f = function (a) {
                    "function" == typeof b && b(new Error("Login Failed: " + a.message))
                };
            c.show(e, f)
        },
        i = function (a, b, c) {
            var d = this,
                e = 1,
                f = !d.anonymousEnabled,
                h = function () {
                    g.call(d, {
                        type: "POST",
                        contentType: "application/json",
                        url: d.apiRoot + "/content",
                        dataType: "json",
                        async: !0,
                        data: JSON.stringify({
                            url: a
                        })
                    }, f, function (a) {
                        b && b(a)
                    }, function (a) {
                        !f && "code" in a && 403 === a.code && 1 === e ? ("object" == typeof console && console.log("Content retrieval in anonymous failed with 403, retrying with userRequired=true"), e++, f = !0, h()) : "function" == typeof c && c(new Error("Content retrieval failed: " + a.message))
                    })
                };
            h()
        },
        j = function (a, b, c) {
            g.call(this, {
                type: "GET",
                url: this.apiRoot + "/contentVersion?includePlainText=false&contentId=" + encodeURIComponent(a),
                dataType: "json",
                async: !0
            }, !this.anonymousEnabled, function (a) {
                if (0 === a.content.length) c && c(new Error("No contentVersion returned"));
                else {
                    var d = a.content[0];
                    if (b) {
                        var e = SPRITZ.model.SpritzText.create(d.sd0, d.sd1, d.sd2, d.duration);
                        b(e)
                    }
                }
            }, function (b) {
                c && c(new Error("Unable to retrieve contentVersion, contentId=" + a + ": " + b.message))
            })
        },
        k = function (a, b) {
            var c = null;
            try {
                this.initialized = !0
            } catch (d) {
                c = d
            }
            null === c ? a && a() : "function" == typeof b && b(new Error("Initialization failed: " + c.message))
        },
        l = function (a, b) {
            r.call(this, a), "function" == typeof b && b(a)
        },
        m = function (a, b, c) {
            t.call(this, a, b, c)
        },
        n = function (a, b) {
            s.call(this, a), b && b(a)
        },
        o = function (a) {
            "undefined" != typeof localStorage && localStorage.removeItem(b(this.apiHost, a))
        },
        p = function (a, c) {
            "undefined" != typeof localStorage && localStorage.setItem(b(this.apiHost, a), c)
        },
        q = function (a) {
            var c;
            return c = "undefined" == typeof localStorage ? null : localStorage.getItem(b(this.apiHost, a))
        },
        r = function (a) {
            this.clientAccessToken = a, null === a ? o.call(this, "client") : p.call(this, "client", a)
        },
        s = function (a) {
            this.userAccessToken = a, null === a ? o.call(this, "user") : p.call(this, "user", a)
        },
        t = function (b, c, d) {
            var e = function (a) {
                    d && d(new Error("Token validation failed: " + a.message))
                },
                f = this;
            a.ajax({
                type: "GET",
                url: this.apiRoot + "/oauth/tokeninfo?access_token=" + encodeURIComponent(b),
                dataType: "json",
                async: !0,
                success: function (a) {
                    a.audience === f.clientId ? n.call(f, b, c, d) : e(new Error("clientId mismatch"))
                },
                error: e
            })
        },
        u = function (a, b, d) {
            this.clientId = a, this.apiRoot = b, this.redirectUri = d, this.initialized = !1, this.clientAccessToken = null, this.userAccessToken = null, this.apiHost = null, this.anonymousEnabled = !1, e.call(this), c.call(this), this.clientAccessToken = q.call(this, "client"), this.userAccessToken = q.call(this, "user")
        };
    return u.prototype.fetchContents = function (a, b, c) {
        var d = this,
            e = function (a) {
                j.call(d, a.id, b, c)
            };
        i.call(this, a, e, c)
    }, u.prototype.isUserLoggedIn = function () {
        return "string" == typeof this.userAccessToken && this.userAccessToken.length > 0
    }, u.prototype.spritzify = function (a, b, c, d) {
        g.call(this, {
            type: "POST",
            url: this.apiRoot + "/misc/spritzify",
            dataType: "json",
            async: !0,
            data: {
                plainText: a,
                locale: b
            }
        }, !0, function (a) {
            try {
                var b = SPRITZ.model.SpritzText.create(a.sd0, a.sd1, a.sd2, a.duration);
                "function" == typeof c && c(b)
            } catch (e) {
                "function" == typeof d && d(new Error("Invalid data received: " + e.message))
            }
        }, function (a) {
            if (d) {
                var b;
                b = "undefined" != typeof a.message ? a.message : a.status + " [" + a.statusText + "]", d(new Error("Unable to retrive spritzableText: " + b))
            }
        })
    }, u.prototype.setAnonymousEnabled = function (a) {
        this.anonymousEnabled = a === !0
    }, {
        SpritzClient: u
    }
}), jQuery(document).ready(function () {
    if ("SpritzSettings" in window && "object" == typeof window.SpritzSettings) if (window.SpritzSettings.hasOwnProperty("clientId")) {
        var a = "https://api.spritzinc.com/api-server/v1/",
            b = location.href;
        window.SpritzSettings.hasOwnProperty("apiRoot") && (a = window.SpritzSettings.apiRoot), window.SpritzSettings.hasOwnProperty("redirectUri") && (b = window.SpritzSettings.redirectUri), "object" == typeof SPRITZ && "object" == typeof SPRITZ.client && (window.SpritzClient = new SPRITZ.client.SpritzClient(window.SpritzSettings.clientId, a, b), window.SpritzSettings.hasOwnProperty("anonymousEnabled") && "boolean" == typeof window.SpritzSettings.anonymousEnabled && window.SpritzClient.setAnonymousEnabled(window.SpritzSettings.anonymousEnabled), "object" == typeof SPRITZ.spritzinc && SPRITZ.spritzinc.initSpritzers(window.SpritzClient))
    } else console && console.log("SpritzSettings is missing clientId property, skipping initialization")
}), SPRITZ.namespace("SPRITZ.spritzinc"), SPRITZ.addToNamespace(SPRITZ.spritzinc, function () {
    function a(a) {
        this.spritzClient = null, this.loading = !1, this.container = null, this.parentContainer = null, this.spritzPanel = null, this.pausePlayBtn = null, this.rewindBtn = null, this.backBtn = null, this.forwardBtn = null, this.pauseTitle = null, this.playTitle = null, this.speedDropdownController = null, this.progressReporter = null, b.call(this, a)
    }
    function b(a) {
        var b = z(a);
        this.container = F(A(b));
        var c = F(".spritz-canvas", this.container);
        this.spritzPanel = new SPRITZ.display.SpritzReadingPanel(c[0]), this.pausePlayBtn = F(".btn-pauseplay", this.container), this.rewindBtn = F(".btn-rewind", this.container), this.backBtn = F(".btn-back", this.container), this.forwardBtn = F(".btn-forward", this.container);
        for (var d = {
            pauseplay: f,
            rewind: g,
            back: h,
            forward: i,
            redicle: j,
            complete: k,
            speedchange: l
        }, e = b.controlButtons, m = 0, n = e.length; n > m; m += 1) F(".btn-" + e[m], this.container).on("click", jsutil.createDelegate(this, d[e[m]]));
        c.on("click", jsutil.createDelegate(this, j));
        var o = F(".btn-group.speed", this.container);
        this.speedDropdownController = new SPRITZ.component.BootstrapSelectController(o), o.on("change", jsutil.createDelegate(this, l));
        var p = y();
        this.spritzPanel.setCurrentTextSpeed(p > 0 ? p : b.defaultSpeed), this.progressReporter = jsutil.createDelegate(this, E), v.call(this), w.call(this, !0), this.pauseTitle = b.controlTitles.pause, this.playTitle = b.controlTitles.play
    }
    function c() {
        if (!this.loading) {
            var a = this.parentContainer.data("url");
            null == a && (a = location.href), this.loading = !0, this.pausePlayBtn.addClass("btn-disabled"), this.spritzClient.fetchContents(a, jsutil.createDelegate(this, e), jsutil.createDelegate(this, d))
        }
    }
    function d(a) {
        F("#spritzer-loading").hide(), this.loading = !1, this.pausePlayBtn.removeClass("btn-disabled"), "object" == typeof console && console.log("Unable to spritz: " + a.message)
    }
    function e(a) {
        F("#spritzer-loading").hide(), this.loading = !1, this.pausePlayBtn.removeClass("btn-disabled"), this.startSpritzing(a), v.call(this, !0), w.call(this, !1)
    }
    function f() {
        null == this.spritzPanel.getCurrentText() ? c.call(this) : x(this.pausePlayBtn) && (this.spritzPanel.isPaused() || this.spritzPanel.isReady() ? (this.spritzPanel.resumeText(), v.call(this, !0), w.call(this, !1), n.call(this, this.playTitle)) : (this.spritzPanel.pauseText(), v.call(this), w.call(this, !0), n.call(this, this.pauseTitle)))
    }
    function g() {
        if (x(this.rewindBtn)) {
            this.spritzPanel.pauseText();
            var a = this.spritzPanel.getCurrentPosition();
            this.spritzPanel.goToFirstWord(), v.call(this), w.call(this, !0), o.call(this, a)
        }
    }
    function h() {
        if (x(this.backBtn)) {
            this.spritzPanel.pauseText();
            var a = this.spritzPanel.getCurrentPosition();
            this.spritzPanel.goToPreviousSentence(), v.call(this), w.call(this, !0), p.call(this, a)
        }
    }
    function i() {
        if (x(this.forwardBtn)) {
            this.spritzPanel.pauseText();
            var a = this.spritzPanel.getCurrentPosition();
            this.spritzPanel.goToNextSentence(), v.call(this), w.call(this, !0), q.call(this, a)
        }
    }
    function j() {
        null == this.spritzPanel.getCurrentText() ? null !== this.spritzClient && c.call(this) : x(this.pausePlayBtn) && (this.spritzPanel.isPaused() || this.spritzPanel.isReady() ? (this.spritzPanel.resumeText(), v.call(this, !0), w.call(this, !1), r.call(this, "Play")) : (this.spritzPanel.pauseText(), v.call(this), w.call(this, !0), r.call(this, "Pause")))
    }
    function k() {
        v.call(this), w.call(this, !0), s.call(this)
    }
    function l(a, b) {
        this.spritzPanel.setCurrentTextSpeed(parseInt(b.newValue)), t.call(this)
    }
    function m(a, b) {
        var c = [];
        c.push(this.spritzPanel.getCurrentPosition()), "undefined" != typeof b && c.push(b), this.parentContainer.trigger("onSpritz" + a, c)
    }
    function n(a) {
        m.call(this, a)
    }
    function o(a) {
        m.call(this, "Rewind", a)
    }
    function p(a) {
        m.call(this, "Back", a)
    }
    function q(a) {
        m.call(this, "Forward", a)
    }
    function r(a) {
        m.call(this, a)
    }
    function s() {
        this.parentContainer.trigger("onSpritzComplete")
    }
    function t() {
        this.parentContainer.trigger("onSpritzSpeedChange", [this.spritzPanel.getCurrentTextSpeed()])
    }
    function u(a) {
        this.parentContainer.trigger("onProgressChange", [a])
    }
    function v(a) {
        null == this.spritzPanel.getCurrentText() || 0 == this.spritzPanel.getCurrentText().getWords().length ? (this.pausePlayBtn.addClass("btn-disabled"), this.rewindBtn.addClass("btn-disabled"), this.backBtn.addClass("btn-disabled"), this.forwardBtn.addClass("btn-disabled")) : a || this.spritzPanel.getCurrentPosition() > 0 && this.spritzPanel.getCurrentText().hasNextWord() ? (this.pausePlayBtn.removeClass("btn-disabled"), this.rewindBtn.removeClass("btn-disabled"), this.backBtn.removeClass("btn-disabled"), this.forwardBtn.removeClass("btn-disabled")) : 0 == this.spritzPanel.getCurrentPosition() ? (this.pausePlayBtn.removeClass("btn-disabled"), this.rewindBtn.addClass("btn-disabled"), this.backBtn.addClass("btn-disabled"), this.forwardBtn.removeClass("btn-disabled")) : this.spritzPanel.getCurrentText().hasNextWord() || (this.pausePlayBtn.addClass("btn-disabled"), this.rewindBtn.removeClass("btn-disabled"), this.backBtn.removeClass("btn-disabled"), this.forwardBtn.addClass("btn-disabled"))
    }
    function w(a) {
        a ? (this.pausePlayBtn.removeClass("btn-pause"), this.pausePlayBtn.addClass("btn-play"), this.pausePlayBtn.prop("title", "Play")) : (this.pausePlayBtn.removeClass("btn-play"), this.pausePlayBtn.addClass("btn-pause"), this.pausePlayBtn.prop("title", "Pause"))
    }
    function x(a) {
        return !a.hasClass("btn-disabled")
    }
    function y() {
        var a = F(".btn-group.speed", this.container).text();
        return parseInt(a.substr(0, a.indexOf("wpm")))
    }
    function z(a) {
        var b = {
            redicleWidth: 340,
            redicleHeight: 70,
            defaultSpeed: 250,
            speedItems: [250, 300, 350, 400, 450, 500, 550, 600],
            controlButtons: ["back", "pauseplay", "rewind"],
            controlTitles: {
                pause: "Pause",
                play: "Play",
                rewind: "Rewind",
                back: "Previous Sentence"
            }
        };
        if ("undefined" != typeof a) {
            for (var c in a) b.hasOwnProperty(c) ? b[c] = a[c] : console.log("?? Invalid attribute [" + c + " : " + a[c] + "] encountered in custom options");
            b.redicleWidth < 250 && (b.redicleWidth = 250), b.redicleHeight < 50 && (b.redicleHeight = 50)
        }
        return b
    }
    function A(a) {
        var b = "";
        return b += '<div class="spritzer-container">', b += '<div id="spritzer-loading"></div>', b += '<div class="spritzer-preloaded button btn-pause"></div>', b += '<div class="canvas-container">', b += '<canvas class="spritz-canvas" width="' + a.redicleWidth + '" height="' + a.redicleHeight + '"></canvas>', b += "</div>", b += '<div class="controls-container">', b += '<a href="http://www.spritzinc.com" target="_blank" class="image powered-by-spritz"></a>', b += C(a), b += B(a), b += "</div>", b += "</div>"
    }
    function B(a) {
        var b = "";
        b += '<div class="button-container">', b += '<div class="spritzer-preload button btn-pause">a</div>';
        for (var c = 0, d = a.controlButtons.length; d > c; c += 1) {
            var e = a.controlButtons[c],
                f = a.controlTitles[e];
            b += '<div class="button btn-' + e + '" title="' + f + '"></div>'
        }
        return b += '<div class="clear"></div>', b += "</div>"
    }
    function C(a) {
        var b = a.speedItems,
            c = "";
        c += '<div class="btn-group speed" data-role="select">', c += '<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown">' + a.defaultSpeed + 'wpm <span class="caret"></span></button>', c += '<ul class="dropdown-menu dropdown-speed" role="menu">';
        for (var d = 0, e = b.length; e > d; d += 1) c += '<li data-value="' + b[d] + '"><a href="#">' + b[d] + "wpm</a></li>";
        return c += "</div>"
    }
    function D(a) {
        var b = F('*[data-role="spritzer"]');
        b.each(function (b, c) {
            var d = F(c),
                e = d.data("options");
            "object" != typeof e && (e = null);
            var f = new SPRITZ.spritzinc.SpritzerController(e);
            f.spritzClient = a, f.attach(d)
        })
    }
    function E(a, b) {
        var c = 100 * a,
            d = c / b;
        100 >= b ? u.call(this, d) : 100 > c % b && u.call(this, d)
    }
    var F = jQuery;
    return a.prototype.attach = function (a) {
        this.parentContainer = a, a.hasClass("spritzer") || a.addClass("spritzer"), this.container.appendTo(a), a.data("controller", this), null != this.spritzClient && (w.call(this, !0), this.pausePlayBtn.removeClass("btn-disabled"))
    }, a.prototype.detach = function () {
        this.container.detach()
    }, a.prototype.startSpritzing = function (a) {
        this.spritzPanel.pauseText(), null == a.getProgressTracker() && a.setProgressTracker(this.progressReporter), this.spritzPanel.displayText(a, jsutil.createDelegate(this, k)), v.call(this, !0), w.call(this, !1)
    }, a.prototype.stopSpritzing = function () {
        this.spritzPanel.resetText(), v.call(this), w.call(this, !0)
    }, a.prototype.pauseSpritzing = function () {
        this.spritzPanel.pauseText()
    }, a.prototype.resumeSpritzing = function () {
        this.spritzPanel.resumeText()
    }, a.prototype.getSpeed = function () {
        return this.spritzPanel.getCurrentTextSpeed()
    }, a.prototype.setSpeed = function (a) {
        return this.speedDropdownController.setValue(a, !1)
    }, a.prototype.getHighlightBestLetter = function () {
        return this.spritzPanel.getHighlightBestLetter()
    }, a.prototype.setHighlightBestLetter = function (a) {
        this.spritzPanel.setHighlightBestLetter(a)
    }, a.prototype.getProgressReporter = function () {
        return this.progressReporter
    }, a.prototype.setProgressReporter = function (a) {
        this.progressReporter = a
    }, {
        SpritzerController: a,
        initSpritzers: D
    }
});
var goog = {};
goog.math = {}, goog.math.Long = function (a, b) {
    this.low_ = 0 | a, this.high_ = 0 | b
}, goog.math.Long.IntCache_ = {}, goog.math.Long.fromInt = function (a) {
    if (a >= -128 && 128 > a) {
        var b = goog.math.Long.IntCache_[a];
        if (b) return b
    }
    var c = new goog.math.Long(0 | a, 0 > a ? -1 : 0);
    return a >= -128 && 128 > a && (goog.math.Long.IntCache_[a] = c), c
}, goog.math.Long.fromNumber = function (a) {
    return isNaN(a) || !isFinite(a) ? goog.math.Long.ZERO : a <= -goog.math.Long.TWO_PWR_63_DBL_ ? goog.math.Long.MIN_VALUE : a + 1 >= goog.math.Long.TWO_PWR_63_DBL_ ? goog.math.Long.MAX_VALUE : 0 > a ? goog.math.Long.fromNumber(-a).negate() : new goog.math.Long(a % goog.math.Long.TWO_PWR_32_DBL_ | 0, a / goog.math.Long.TWO_PWR_32_DBL_ | 0)
}, goog.math.Long.fromBits = function (a, b) {
    return new goog.math.Long(a, b)
}, goog.math.Long.fromString = function (a, b) {
    if (0 == a.length) throw Error("number format error: empty string");
    var c = b || 10;
    if (2 > c || c > 36) throw Error("radix out of range: " + c);
    if ("-" == a.charAt(0)) return goog.math.Long.fromString(a.substring(1), c).negate();
    if (a.indexOf("-") >= 0) throw Error('number format error: interior "-" character: ' + a);
    for (var d = goog.math.Long.fromNumber(Math.pow(c, 8)), e = goog.math.Long.ZERO, f = 0; f < a.length; f += 8) {
        var g = Math.min(8, a.length - f),
            h = parseInt(a.substring(f, f + g), c);
        if (8 > g) {
            var i = goog.math.Long.fromNumber(Math.pow(c, g));
            e = e.multiply(i).add(goog.math.Long.fromNumber(h))
        } else e = e.multiply(d), e = e.add(goog.math.Long.fromNumber(h))
    }
    return e
}, goog.math.Long.TWO_PWR_16_DBL_ = 65536, goog.math.Long.TWO_PWR_24_DBL_ = 1 << 24, goog.math.Long.TWO_PWR_32_DBL_ = goog.math.Long.TWO_PWR_16_DBL_ * goog.math.Long.TWO_PWR_16_DBL_, goog.math.Long.TWO_PWR_31_DBL_ = goog.math.Long.TWO_PWR_32_DBL_ / 2, goog.math.Long.TWO_PWR_48_DBL_ = goog.math.Long.TWO_PWR_32_DBL_ * goog.math.Long.TWO_PWR_16_DBL_, goog.math.Long.TWO_PWR_64_DBL_ = goog.math.Long.TWO_PWR_32_DBL_ * goog.math.Long.TWO_PWR_32_DBL_, goog.math.Long.TWO_PWR_63_DBL_ = goog.math.Long.TWO_PWR_64_DBL_ / 2, goog.math.Long.ZERO = goog.math.Long.fromInt(0), goog.math.Long.ONE = goog.math.Long.fromInt(1), goog.math.Long.NEG_ONE = goog.math.Long.fromInt(-1), goog.math.Long.MAX_VALUE = goog.math.Long.fromBits(-1, 2147483647), goog.math.Long.MIN_VALUE = goog.math.Long.fromBits(0, -2147483648), goog.math.Long.TWO_PWR_24_ = goog.math.Long.fromInt(1 << 24), goog.math.Long.prototype.toInt = function () {
    return this.low_
}, goog.math.Long.prototype.toNumber = function () {
    return this.high_ * goog.math.Long.TWO_PWR_32_DBL_ + this.getLowBitsUnsigned()
}, goog.math.Long.prototype.toString = function (a) {
    var b = a || 10;
    if (2 > b || b > 36) throw Error("radix out of range: " + b);
    if (this.isZero()) return "0";
    if (this.isNegative()) {
        if (this.equals(goog.math.Long.MIN_VALUE)) {
            var c = goog.math.Long.fromNumber(b),
                d = this.div(c),
                e = d.multiply(c).subtract(this);
            return d.toString(b) + e.toInt().toString(b)
        }
        return "-" + this.negate().toString(b)
    }
    for (var f = goog.math.Long.fromNumber(Math.pow(b, 6)), e = this, g = "";;) {
        var h = e.div(f),
            i = e.subtract(h.multiply(f)).toInt(),
            j = i.toString(b);
        if (e = h, e.isZero()) return j + g;
        for (; j.length < 6;) j = "0" + j;
        g = "" + j + g
    }
}, goog.math.Long.prototype.getHighBits = function () {
    return this.high_
}, goog.math.Long.prototype.getLowBits = function () {
    return this.low_
}, goog.math.Long.prototype.getLowBitsUnsigned = function () {
    return this.low_ >= 0 ? this.low_ : goog.math.Long.TWO_PWR_32_DBL_ + this.low_
}, goog.math.Long.prototype.getNumBitsAbs = function () {
    if (this.isNegative()) return this.equals(goog.math.Long.MIN_VALUE) ? 64 : this.negate().getNumBitsAbs();
    for (var a = 0 != this.high_ ? this.high_ : this.low_, b = 31; b > 0 && 0 == (a & 1 << b); b--);
    return 0 != this.high_ ? b + 33 : b + 1
}, goog.math.Long.prototype.isZero = function () {
    return 0 == this.high_ && 0 == this.low_
}, goog.math.Long.prototype.isNegative = function () {
    return this.high_ < 0
}, goog.math.Long.prototype.isOdd = function () {
    return 1 == (1 & this.low_)
}, goog.math.Long.prototype.equals = function (a) {
    return this.high_ == a.high_ && this.low_ == a.low_
}, goog.math.Long.prototype.notEquals = function (a) {
    return this.high_ != a.high_ || this.low_ != a.low_
}, goog.math.Long.prototype.lessThan = function (a) {
    return this.compare(a) < 0
}, goog.math.Long.prototype.lessThanOrEqual = function (a) {
    return this.compare(a) <= 0
}, goog.math.Long.prototype.greaterThan = function (a) {
    return this.compare(a) > 0
}, goog.math.Long.prototype.greaterThanOrEqual = function (a) {
    return this.compare(a) >= 0
}, goog.math.Long.prototype.compare = function (a) {
    if (this.equals(a)) return 0;
    var b = this.isNegative(),
        c = a.isNegative();
    return b && !c ? -1 : !b && c ? 1 : this.subtract(a).isNegative() ? -1 : 1
}, goog.math.Long.prototype.negate = function () {
    return this.equals(goog.math.Long.MIN_VALUE) ? goog.math.Long.MIN_VALUE : this.not().add(goog.math.Long.ONE)
}, goog.math.Long.prototype.add = function (a) {
    var b = this.high_ >>> 16,
        c = 65535 & this.high_,
        d = this.low_ >>> 16,
        e = 65535 & this.low_,
        f = a.high_ >>> 16,
        g = 65535 & a.high_,
        h = a.low_ >>> 16,
        i = 65535 & a.low_,
        j = 0,
        k = 0,
        l = 0,
        m = 0;
    return m += e + i, l += m >>> 16, m &= 65535, l += d + h, k += l >>> 16, l &= 65535, k += c + g, j += k >>> 16, k &= 65535, j += b + f, j &= 65535, goog.math.Long.fromBits(l << 16 | m, j << 16 | k)
}, goog.math.Long.prototype.subtract = function (a) {
    return this.add(a.negate())
}, goog.math.Long.prototype.multiply = function (a) {
    if (this.isZero()) return goog.math.Long.ZERO;
    if (a.isZero()) return goog.math.Long.ZERO;
    if (this.equals(goog.math.Long.MIN_VALUE)) return a.isOdd() ? goog.math.Long.MIN_VALUE : goog.math.Long.ZERO;
    if (a.equals(goog.math.Long.MIN_VALUE)) return this.isOdd() ? goog.math.Long.MIN_VALUE : goog.math.Long.ZERO;
    if (this.isNegative()) return a.isNegative() ? this.negate().multiply(a.negate()) : this.negate().multiply(a).negate();
    if (a.isNegative()) return this.multiply(a.negate()).negate();
    if (this.lessThan(goog.math.Long.TWO_PWR_24_) && a.lessThan(goog.math.Long.TWO_PWR_24_)) return goog.math.Long.fromNumber(this.toNumber() * a.toNumber());
    var b = this.high_ >>> 16,
        c = 65535 & this.high_,
        d = this.low_ >>> 16,
        e = 65535 & this.low_,
        f = a.high_ >>> 16,
        g = 65535 & a.high_,
        h = a.low_ >>> 16,
        i = 65535 & a.low_,
        j = 0,
        k = 0,
        l = 0,
        m = 0;
    return m += e * i, l += m >>> 16, m &= 65535, l += d * i, k += l >>> 16, l &= 65535, l += e * h, k += l >>> 16, l &= 65535, k += c * i, j += k >>> 16, k &= 65535, k += d * h, j += k >>> 16, k &= 65535, k += e * g, j += k >>> 16, k &= 65535, j += b * i + c * h + d * g + e * f, j &= 65535, goog.math.Long.fromBits(l << 16 | m, j << 16 | k)
}, goog.math.Long.prototype.div = function (a) {
    if (a.isZero()) throw Error("division by zero");
    if (this.isZero()) return goog.math.Long.ZERO;
    if (this.equals(goog.math.Long.MIN_VALUE)) {
        if (a.equals(goog.math.Long.ONE) || a.equals(goog.math.Long.NEG_ONE)) return goog.math.Long.MIN_VALUE;
        if (a.equals(goog.math.Long.MIN_VALUE)) return goog.math.Long.ONE;
        var b = this.shiftRight(1),
            c = b.div(a).shiftLeft(1);
        if (c.equals(goog.math.Long.ZERO)) return a.isNegative() ? goog.math.Long.ONE : goog.math.Long.NEG_ONE;
        var d = this.subtract(a.multiply(c)),
            e = c.add(d.div(a));
        return e
    }
    if (a.equals(goog.math.Long.MIN_VALUE)) return goog.math.Long.ZERO;
    if (this.isNegative()) return a.isNegative() ? this.negate().div(a.negate()) : this.negate().div(a).negate();
    if (a.isNegative()) return this.div(a.negate()).negate();
    for (var f = goog.math.Long.ZERO, d = this; d.greaterThanOrEqual(a);) {
        for (var c = Math.max(1, Math.floor(d.toNumber() / a.toNumber())), g = Math.ceil(Math.log(c) / Math.LN2), h = 48 >= g ? 1 : Math.pow(2, g - 48), i = goog.math.Long.fromNumber(c), j = i.multiply(a); j.isNegative() || j.greaterThan(d);) c -= h, i = goog.math.Long.fromNumber(c), j = i.multiply(a);
        i.isZero() && (i = goog.math.Long.ONE), f = f.add(i), d = d.subtract(j)
    }
    return f
}, goog.math.Long.prototype.modulo = function (a) {
    return this.subtract(this.div(a).multiply(a))
}, goog.math.Long.prototype.not = function () {
    return goog.math.Long.fromBits(~this.low_, ~this.high_)
}, goog.math.Long.prototype.and = function (a) {
    return goog.math.Long.fromBits(this.low_ & a.low_, this.high_ & a.high_)
}, goog.math.Long.prototype.or = function (a) {
    return goog.math.Long.fromBits(this.low_ | a.low_, this.high_ | a.high_)
}, goog.math.Long.prototype.xor = function (a) {
    return goog.math.Long.fromBits(this.low_ ^ a.low_, this.high_ ^ a.high_)
}, goog.math.Long.prototype.shiftLeft = function (a) {
    if (a &= 63, 0 == a) return this;
    var b = this.low_;
    if (32 > a) {
        var c = this.high_;
        return goog.math.Long.fromBits(b << a, c << a | b >>> 32 - a)
    }
    return goog.math.Long.fromBits(0, b << a - 32)
}, goog.math.Long.prototype.shiftRight = function (a) {
    if (a &= 63, 0 == a) return this;
    var b = this.high_;
    if (32 > a) {
        var c = this.low_;
        return goog.math.Long.fromBits(c >>> a | b << 32 - a, b >> a)
    }
    return goog.math.Long.fromBits(b >> a - 32, b >= 0 ? 0 : -1)
}, goog.math.Long.prototype.shiftRightUnsigned = function (a) {
    if (a &= 63, 0 == a) return this;
    var b = this.high_;
    if (32 > a) {
        var c = this.low_;
        return goog.math.Long.fromBits(c >>> a | b << 32 - a, b >>> a)
    }
    return 32 == a ? goog.math.Long.fromBits(b, 0) : goog.math.Long.fromBits(b >>> a - 32, 0)
};