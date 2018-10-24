/* ADMINISTRATOR SETTINGS */

$(document).ready(function () {
    $( ".movedownNavigation" ).click(function(e) {
        var element = $(e.target).closest('.headerelement');
        element.insertAfter(element.next());
        var arr = Array();
        $('.headerelement').each(function(){
            arr.push($(this).attr('id').replace('order', ''));
        })
        $.post( "settings/processNavigationOrder", { order: arr.toString() } );
    });
    $( ".moveupNavigation" ).click(function(e) {
        var element = $(e.target).closest('.headerelement');
        element.insertBefore(element.prev());
        var arr = Array();
        $('.headerelement').each(function(){
            arr.push($(this).attr('id').replace('order', ''));
        })
        $.post( "settings/processNavigationOrder", { order: arr.toString() } );
    });
    $('#editHeader').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var type = form.find('.type').val();
        var id = form.find('.id').val();
        $('#edithtml').val(id);
        if( type == 'HTML') {
            $('#editheadercontenthtml').val($('#content-' + id).html());
            $('#editimagebody').hide();
            $('#edithtmlbody').show();
        }
        if( type == 'IMAGE') {
            $('#editheadercontentimage').val($('#content-' + id).attr('src'));
            $('#editimagebody').show();
            $('#edithtmlbody').hide();
        }
        $('#headeredit').val(id);
    });
    $('#editPTtileback').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var backt = form.find('.backt').val();
        var id = form.find('.id').val();
        $('#editPTbackid').val(id);
        var editor = ace.edit("editorb");
        editor.setTheme("ace/theme/twilight");
        editor.setOptions({
            maxLines: Infinity,
            minLines: 20
        });
        editor.getSession().setMode("ace/mode/html");
        editor.setValue(backt);
    });
    $('#editPTtilefront').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var front = form.find('.front').val();
        var id = form.find('.id').val();
        $('#editPTfrontid').val(id);
        var editor = ace.edit("editorf");
        editor.setTheme("ace/theme/twilight");
        editor.setOptions({
            maxLines: Infinity,
            minLines: 20
        });
        editor.getSession().setMode("ace/mode/html");
        editor.setValue(front);
    });
    $('#updateLink').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var type = form.find('.type').val();
        var id = form.find('.id').val();
        var name = form.find('.name').val();
        var link = form.find('.link').val();
        $('#editlink').val(id);
        $('#linkname').val(name);
        $('#linkurl').val(link);
    });
    $('#changeColor').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var id = form.find('.id').val();
        var c = form.find('.backc').val();
        $('#bgcolorid').val(id);
        $('#color').val(c);
        $('.bgcolor').colorpicker();
        if(c.length == 0) {
            $('#colorInfo').text("Currently no color is forced on the projects of this type!");
            $('.bgcolor').colorpicker({ 'setValue': '#000000', format: 'hex'});
        } else {
            $('#colorInfo').text("The color below is currently forced on all projects of this type!");
            $('.bgcolor').colorpicker({ 'setValue': c, format: 'hex'});
        }
    });
    $('#changeColor').on('hidden.bs.modal', function (e) {
        //$('#color').val("");
        //$('.bgcolor').colorpicker('destroy');
        location.reload();
    });

    var messageEventListenerAdded;
    $( ".browse" ).click(function() {
        var width = '80%';
        var height = '70%';

        if(!popup( repourl + "/index.html",width,height))
            alert("Please enable Popups to use this feature!");

        if(!messageEventListenerAdded) {
            window.addEventListener("message", receiveMessage, false);
            messageEventListenerAdded = true;
        }

    });
    function receiveMessage(event) {
        $(".headerimage").val(event.data);
    }
    $( ".movedownCarousel" ).click(function(e) {
        var element = $(e.target).closest('.headerelement');
        element.insertAfter(element.next());
        var arr = Array();
        $('.headerelement').each(function(){
            arr.push($(this).attr('id').replace('order', ''));
        })
        $.post( "settings/processHeaderOrder", { order: arr.toString() } );
    });
    $( ".moveupCarousel" ).click(function(e) {
        var element = $(e.target).closest('.headerelement');
        element.insertBefore(element.prev());
        var arr = Array();
        $('.headerelement').each(function(){
            arr.push($(this).attr('id').replace('order', ''));
        })
        $.post( "settings/processHeaderOrder", { order: arr.toString() } );
    });
    $(function() {
        $( "#sortable" ).sortable();
        $( "#sortable" ).disableSelection();
    });

    $('body').on('click', '.ajaxclickable', function (){
        if( !$(this).hasClass("disabled"))
            $( this ).toggleClass( "active" );
    });
    $('body').on('click', '.ajaxsingleclickable', function (){
        $( ".ajaxsingleclickable").removeClass("active");
        $( ".ajaxsingleclickable").removeClass("activated");
        $( this ).addClass( "active" );
        $( this ).addClass( "activated" );
    });
    $('#addProject').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var id = form.find('.id').val();
        $.get( "settings/ajaxPTProjects?id=" + id, function( data ) {
            $( "#projectResult" ).html( data );
        });
    });
    $('#editEndpoints').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var id = form.find('.id').val();
        $.get( "settings/ajaxEndpoints?id=" + id, function( data ) {
            $( "#endpointAjaxContent" ).html( data );
        });
    });

    $( document ).on( "click", ".endpointClickable", function() {
        var id = $(this).attr("id");
        $.get( "settings/ajaxEndpoint?id=" + id, function( data ) {
            $( "#endpointAjaxContent" ).html( data );
        });
    });

    $( document ).on( "click", ".proposalClickable", function() {
        var id = $(this).attr("id");
        $.get( "settings/ajaxProposal?id=" + id, function( data ) {
            $( "#proposalAjaxContent" ).html( data );
        });
    });

    $( document ).on( "click", ".Wclickable", function() {
        var dbniid = $(this).attr("id");
        var typeid = $("#typeid").val();
        $.get( "settings/ajaxWConfInstantiation?id=" + typeid + "&dbni=" + dbniid, function( data ) {
            $( "#serviceWizardResult" ).html( data );
        });
    });

    $( document ).on( "click", ".proposetClickable", function() {
        $( "#proposalscontent" ).html( $( "#muviemotproposal" ).html() );
    });

    $( ".propClickable" ).click(function() {
        $( ".right").append($(this).html());
    });

    $( "li" ).click(function() {
        if( !$(this).hasClass("disabled"))
            $( this ).toggleClass( "active" );
    });

    $('#editEndpoint').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var id = form.find('.id').val();
        $.get( "settings/ajaxEndpoints?id=" + id, function( data ) {
            $( "#endpointAjaxContent" ).html( data );
        });
    });


    $('#editProposals').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var id = form.find('.id').val();
        $.get( "settings/ajaxProposals?id=" + id, function( data ) {
            $( "#proposalAjaxContent" ).html( data );
        });
    });


    $('#updateTypes').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var id = form.find('.id').val();
        var name = form.find('.name').val();
        var link = form.find('.link').val();
        var caption = form.find('.caption').val();
        var label = form.find('.label').val();
        $('#ptupdateid').val(id);
        $('#ptupdatename').val(name);
        $('#ptupdateurl').val(link);
        $('#ptupdatecaption').val(caption);
        $('#ptupdatelabel').val(label);


        $.get( "settings/ajaxPT?id=" + id, function( data ) {
            $( "#ptResult" ).html( data );
        });
    });
    $('#updateProject').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var id = form.find('.id').val();
        var name = form.find('.name').val();
        var link = form.find('.link').val();
        $('#projupdateid').val(id);
        $('#projupdatename').val(name);
        $('#projupdateurl').val(link);

        $.get( "settings/ajaxProj?id=" + id, function( data ) {
            $( "#projResult" ).html( data );
        });
    });
    $('#addService').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var id = form.find('.id').val();
        $.get( "settings/ajaxPTServices?id=" + id, function( data ) {
            $( "#serviceResult" ).html( data );
        });
    });
    $('#confWizard').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var id = form.find('.id').val();
        $.get( "settings/ajaxWConfEndpoints?id=" + id, function( data ) {
            $( "#serviceWizardResult" ).html( data );
        });
    });
    $('#createOverlay').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var id = form.find('.id').val();
        $.get( "settings/ajaxPTOverlay?id=" + id, function( data ) {
            $( "#overlayResult" ).html( data );
        });
    });

    $('#addEndpoint').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('section');
        var id = form.find('.id').val();
        $("#serviceendpointid").val(id);
    });
    $("#confbtn").addClass("disabled");
    $( "#conftext" ).bind("change paste keyup", function() {
        if( $( "#conftext").val() == "Yes, I know!" ) {
            $("#confbtn").removeClass("disabled");
        } else {
            $("#confbtn").addClass("disabled");
        }
    });
    $( ".abortbtn" ).click(function() {
        $( "#conftext").val("");
        $("#confbtn").addClass("disabled");
    });
    $('#deleteConfirm').on('show.bs.modal', function (e) {
        var form = $(e.relatedTarget).closest('div');
        $("#modalid").val(form.find('input').val());
        var modalid = $("#modalid").val();
        $.get( "settings/ajaxProjectUsage?id=" + modalid, function( data ) {
            $( "#ajaxResult" ).html( data );
        });
    });



});