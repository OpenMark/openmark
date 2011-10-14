
	tinyMCE.init({
		// General options
		mode : "textareas",
		editor_selector : "[EDITOR_SELECTOR]",
		theme :  "advanced",	
		document_base_url : "tiny_mce/",
		apply_source_formatting : true,
		directionality : "ltr",
		[READ_ONLY],
		elements : "[ELEMENTS]",
	    entity_encoding : "raw",
	    forced_root_block : false,
	    force_br_newlines : true,
	    force_p_newlines : false,
	    language : "en",
	    nowrap : true,
	    plugins : "-supsub",
	    height : "[HEIGHT]",
	    width : "[WIDTH]",
	    relative_urls :  false,
	    remove_script_host :  false,
	    skin : "o2k7",
	    skin_variant : "silver",
	    theme_advanced_layout_manager : "simple",
	    theme_advanced_toolbar_align : "left",
	    theme_advanced_buttons1 : "[BUTTONS]",
	    theme_advanced_buttons2 : "",
	    theme_advanced_buttons3 : "",
	    theme_advanced_resize_horizontal : true,
	    theme_advanced_resizing :  true,
	    theme_advanced_toolbar_location : "top",
	    theme_advanced_statusbar_location : "none",
	    valid_elements : "[VALID_ELEMENTS]",
	    valid_children : "body[sup|sub|#text],sup[#text],sub[#text]"
	});
