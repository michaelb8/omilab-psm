/*
 MIT

 all right I have no idea about MIT license, but put it on seems cool. :P  Enjoy!
*/
CKEDITOR.plugins.add("wenzgmap",{icons:"wenzgmap",init:function(a){a.addCommand("wenzgmapDialog",new CKEDITOR.dialogCommand("wenzgmapDialog"));a.ui.addButton("wenzgmap",{label:"Insert a google map",command:"wenzgmapDialog",toolbar:"paragraph"});CKEDITOR.dialog.add("wenzgmapDialog",this.path+"dialogs/wenzgmap.js")}});