$(document).on('menuOrderSaved', function() {
    console.log('메뉴 순서 저장 완료, 메뉴 리스트 새로고침');
    fn_selectMenuList();
});
function fn_selectMenuList() {
    $.ajax({
        url: menuUrl,
        method: 'GET',
        dataType: 'json',
        success: function(data) {
            let menuHtml = '';

            if (data.topMenuList && data.topMenuList.length > 0) {
                $.each(data.topMenuList, function(index, topMenuItem) {
                    // top_menu 값을 소문자로 변환
                    let topMenuLower = topMenuItem.top_menu.toLowerCase();

                    let menuDeleteYn = topMenuItem.delete_yn;
					
                    if (menuDeleteYn === 'Y') {
                    	return true;
                	}
                    if (topMenuItem.top_menu.endsWith('_MNG') && userRole !== 'ADMIN') {
                        return;
                    }
                    
                    let iconHtml = index === 0 ? '<i class="fa-solid fa-cat"></i> ' : '';

                    menuHtml += `
                    <li>
                        <a href="#">${iconHtml}${topMenuItem.top_menu_nm}</a>`;

                    if (topMenuItem.subMenus && topMenuItem.subMenus.length > 0) {
                        menuHtml += '<ul>';
                        $.each(topMenuItem.subMenus, function(subIndex, subMenuItem) {
                            // sub_menu 값도 소문자로 변환
                            let subMenuLower = subMenuItem.sub_menu.toLowerCase();
                            let menuDeleteYn = subMenuItem.delete_yn;
		                    if (menuDeleteYn === 'Y') {
		                    	return true;
		                	}
                            menuHtml += `
                            <li>
                                <a href="#" onclick="fn_link('${topMenuLower}', '${subMenuLower}')">
                                    ${subMenuItem.sub_menu_nm}
                                </a>
                            </li>`;
                        });
                        menuHtml += '</ul>';
                    }

                    menuHtml += '</li>';
                });
            }

            $('#menuList').html(menuHtml || '<li>메뉴가 없습니다.</li>');
        },
        error: function(error) {
            console.error("Error fetching menu:", error);
            alert("메뉴를 불러오는데 실패했습니다.");
        }
    });
}

function fn_link(topMenu, subMenu) {
    if(topMenu.endsWith('_mng')){
        window.location.href = `/admin/${topMenu}/${subMenu}`;
    }else{
        window.location.href = `/user/${topMenu}/${subMenu}`;
    }
}