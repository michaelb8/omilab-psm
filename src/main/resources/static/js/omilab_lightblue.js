function findGetParameter(parameterName) {
    var result = null,
        tmp = [];
    location.search
        .substr(1)
        .split("&")
        .forEach(function (item) {
            tmp = item.split("=");
            if (tmp[0] === parameterName) result = decodeURIComponent(tmp[1]);
        });
    return result;
}
    $(window).on('resize orientationChange', function(event) {
        if ( $(window).width() < 715) {
            $('#projectsidemenu').attr('style', 'width: 100% !important');
        }
        if ( $(window).width() > 715) {
            $('#projectsidemenu').attr('style', '');
        }
    });
    
    $(window).resize(setOmiVideoHeight);

    var psmTempID;
    var psmMessageEventListenerAdded;
    function queryRepository(param) {
        psmTempID = param;
        var width = '80%';
        var height = '70%';

        if(!popup( repourl + "/index.html",width,height))
            alert("Please enable Popups to use this feature!");

        if(!psmMessageEventListenerAdded) {
            window.addEventListener("message", getRepoResponse, false);
            psmMessageEventListenerAdded = true;
        }

    }
    function getRepoResponse(event) {
        $("#" + psmTempID).val(encodeURI(event.data));
    }

    $('body').on('click', '.ptClickable', function (){
        $( ".ptClickable").removeClass("active");
        $( this ).addClass( "active" );
    });

    $(document).ready(function () {

        if ( $(window).width() < 715) {
            $('#projectsidemenu').attr('style', 'width: 100% !important');
        }

        var $body   = $(document.body);
        var navHeight = $('.navbar').outerHeight(true) + 10;

        $body.scrollspy({
            target: '#leftCol',
            offset: navHeight
        });


        $('#headerCarousel').carousel();
        
        setOmiVideoHeight();

        $('#sidebar > a').on('click', function (e) {
            e.preventDefault();
            if(!$(this).hasClass("active")){
                var lastActive = $(this).closest("#sidebar").children(".active");
                lastActive.removeClass("active");
                lastActive.next('div').collapse('hide');
                $(this).addClass("active");
                $(this).next('div').collapse('show');
            }
        });

        $('#sidebar').affix({
            offset: {
                top: 550
            }
        });

        $('.prettySocial').prettySocial();

        $(window).scroll(checkNaviFixed);

        isCookieAgree();

    });

    function checkNaviFixed(){
        if($(window).scrollTop() >= $('#header').outerHeight()){
            // fix navi and set placeholder height
            $('#nav-placeholder').css('height', $('#nav').outerHeight());
            $('#nav').css('position', 'fixed');
            if($('.alert-header').is(':visible')){
                $('#nav').css('top', $('.alert-header').outerHeight());
            }
        } else{
            $('#nav-placeholder').css('height', '0');
            $('#nav').css('position', 'relative');
            $('#nav').css('top', '0');
        }
    }
    
    function setOmiVideoHeight(){
        $('#omi-video').outerHeight($('#omi-video').outerWidth()*0.5625);
    }

    (function(d, s, id) {  var js, fjs = d.getElementsByTagName(s)[0];  if (d.getElementById(id)) return;  js = d.createElement(s); js.id = id;  js.src = "//connect.facebook.net/de_DE/sdk.js#xfbml=1&version=v2.3";  fjs.parentNode.insertBefore(js, fjs);}(document, 'script', 'facebook-jssdk'));

    function doCall(caller) {
        window.location.href=$(caller).attr("href");
    }

    $( "#toggleadmin" ).click(function() {
        toggleAdminView();
    });

    function toggleAdminView() {
        if(readCookie('adminview')) {
            createCookie('adminview','false',30);
            $( "#admincontrol" ).hide();
        }
        else {
            createCookie('adminview','true',30);
            $( "#admincontrol" ).show();
        }
    }

    function checkAdminView() {
        return readCookie('adminview');
    }

    function isCookieAgree(){
        if(!readCookie('cookiesagreed')){
            $('#cookie-notification').show();
        }
    }

    function agreeCookies(){
        createCookie('cookiesagreed', 'true', 365*4);
        $('#cookie-notification').hide();
    }

    function createCookie(name, value, days) {
        var expires;

        if (days) {
            var date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            expires = "; expires=" + date.toGMTString();
        } else {
            expires = "";
        }
        document.cookie = encodeURIComponent(name) + "=" + encodeURIComponent(value) + expires + "; path=/";
    }

    function readCookie(name) {
        var nameEQ = encodeURIComponent(name) + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) === ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) return decodeURIComponent(c.substring(nameEQ.length, c.length));
        }
        return null;
    }

   function popup( url, width, height, options ) {
        width = width || '80%';
        height = height || '70%';

        if ( typeof width == 'string' && width.length > 1 && width.substr( width.length - 1, 1 ) == '%' )
            width = parseInt( window.screen.width * parseInt( width, 10 ) / 100, 10 );

        if ( typeof height == 'string' && height.length > 1 && height.substr( height.length - 1, 1 ) == '%' )
            height = parseInt( window.screen.height * parseInt( height, 10 ) / 100, 10 );

        if ( width < 640 )
            width = 640;

        if ( height < 420 )
            height = 420;

        var top = parseInt( ( window.screen.height - height ) / 2, 10 ),
            left = parseInt( ( window.screen.width - width ) / 2, 10 );

        options = ( options || 'location=no,menubar=no,toolbar=no,dependent=yes,minimizable=no,modal=yes,alwaysRaised=yes,resizable=yes,scrollbars=yes' ) + ',width=' + width +
            ',height=' + height +
            ',top=' + top +
            ',left=' + left;

        var popupWindow = window.open( '', null, options, true );

        // Blocked by a popup blocker.
        if ( !popupWindow )
            return false;

        try {
            // Chrome is problematic with moveTo/resizeTo, but it's not really needed here (#8855).
            var ua = navigator.userAgent.toLowerCase();
            if ( ua.indexOf( ' chrome/' ) == -1 ) {
                popupWindow.moveTo( left, top );
                popupWindow.resizeTo( width, height );
            }
            popupWindow.focus();
            popupWindow.location.href = url;
        } catch ( e ) {
            popupWindow = window.open( url, null, options, true );
        }

        return popupWindow;
    }

    function checkDate(sdd, smm, syyyy, edd, emm, eyyyy){
                var today = new Date();
                var dd = today.getDate();
                var mm = today.getMonth() + 1;
                var yyyy = today.getFullYear();
                var past = 'past';
                var upcoming = 'upcoming';
                var current = 'current';
                if(eyyyy < yyyy){
                    // end year smaller current year
                    return past;
                } else if(syyyy > yyyy){
                    // start year greater current year
                    return upcoming;
                } else if(syyyy == yyyy && eyyyy > yyyy){
                    // start year is current and end year is upcoming
                    if(smm < mm){
                        // started in past month
                        return current;
                    } else if(smm == mm){
                        // start in current month
                        if(sdd <= dd){
                            // started in past day or today
                            return current;
                        } else{
                            return upcoming;
                        }
                    } else{
                        // start in upcoming months
                        return upcoming;
                    }
                } else if(syyyy < yyyy && eyyyy == yyyy){
                    // started in past and end year is current
                    if(emm > mm){
                        // end month greater than current
                        return current;
                    } else if(emm == mm){
                        // end month is this month
                        if(edd >= dd){
                            // end day upcoming or current day
                            return current;
                        } else{
                            return past;
                        }
                    } else{
                        // end month is past
                        return past;
                    }
                } else if(syyyy < yyyy && eyyyy > yyyy){
                    // start past, end upcoming
                    return current;
                } else{
                    if(emm < mm){
                        return past;
                    } else if(smm > mm){
                        return upcoming
                    } else if(smm == mm && emm > mm){
                        if(sdd <= dd){
                            return current;
                        } else{
                            return upcoming;
                        }
                    } else if(smm < mm && emm == mm){
                        if(edd >= dd){
                            return current;
                        } else{
                            return past;
                        }
                    } else if(smm < mm && emm > mm){
                        return current;
                    } else{
                        if(edd < dd){
                            return past;
                        } else if(sdd > dd){
                            return upcoming;
                        } else if(sdd <= dd && edd >= dd){
                            return current;
                        }
                    }
                }
            }