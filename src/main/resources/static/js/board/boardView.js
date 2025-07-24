$(document).ready(function() { 
	
	$('.board_update').click(function() {
		const boardSeq = $(this).data('boardseq');
	    const subMenu = $(this).data('submenu');
		const topMenu = $(this).data('topmenu');
	    fn_menuLink('update',topMenu, subMenu, boardSeq);
	});
	
	$('.board-list-btn').click(function() {
	    const subMenu = $(this).data('submenu');
		const topMenu = $(this).data('topmenu');
	    fn_menuLink('list',topMenu, subMenu);
	});
	
});
function fn_menuLink(_page, topMenu, subMenu, boardSeq = null) {
    let url = '/user';  // let 사용 권장

    if(_page == "list"){
        url += '/' + topMenu + '/' + subMenu;
    }else if (_page == 'update') {
		url += '/' + topMenu + '/' + subMenu + '/update';
		if (boardSeq) {
			url += '?board_seq=' + boardSeq;
			}
	}
    window.location.href = url;
}