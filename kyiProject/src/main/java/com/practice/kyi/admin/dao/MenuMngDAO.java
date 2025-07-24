package com.practice.kyi.admin.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.practice.kyi.admin.dao.vo.SubMenuVO;
import com.practice.kyi.admin.dao.vo.TopMenuVO;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository
public class MenuMngDAO extends EgovAbstractMapper{
	//탑 메뉴
	public List<TopMenuVO> topMenuList(TopMenuVO vo){
		return this.selectList("topMenuDAO.topMenuList", vo);
	}
	public int registTopMenu(TopMenuVO vo) {
		return this.insert("topMenuDAO.registTopMenu",vo);
	}
	public int selectTopMenu(String topMenu) {
		return this.selectOne("topMenuDAO.selectTopMenu",topMenu);
	}
	public int selectTopMenuNm(String topMenuNm) {
		return this.selectOne("topMenuDAO.selectTopMenuNm",topMenuNm);
	}
	public TopMenuVO topMenuForm(String topMenuSeq) {
		return this.selectOne("topMenuDAO.topMenuForm",topMenuSeq);
	}
	public int updateTopMenu(TopMenuVO vo) {
		return this.update("topMenuDAO.updateTopMenu",vo);
	}
	public int activeTopMenu(TopMenuVO vo) {
		return this.update("topMenuDAO.activeTopMenu",vo);
	}
	public int updateTopMenuOrder(TopMenuVO vo) {
		return this.update("topMenuDAO.updateTopMenuOrder",vo);
	}
	
	//서브메뉴
	public List<SubMenuVO> subMenuList(SubMenuVO vo){
		return this.selectList("subMenuDAO.subMenuList", vo);
	}
	public List<TopMenuVO> topMenuNm(TopMenuVO vo){
		return this.selectList("subMenuDAO.topMenuNm", vo);
	}
	public int registSubMenu(SubMenuVO vo) {
		return this.insert("subMenuDAO.registSubMenu",vo);
	}
	public int selectSubMenu(String SubMenu) {
		return this.selectOne("subMenuDAO.selectSubMenu",SubMenu);
	}
	public int selectSubMenuNm(String subMenuNm) {
		return this.selectOne("subMenuDAO.selectSubMenuNm",subMenuNm);
	}
	public SubMenuVO subMenuForm(String subMenuSeq) {
		return this.selectOne("subMenuDAO.subMenuForm",subMenuSeq);
	}
	public int updateSubMenu(SubMenuVO vo) {
		return this.update("subMenuDAO.updateSubMenu",vo);
	}
	public int activeSubMenu(SubMenuVO vo) {
		return this.update("subMenuDAO.activeSubMenu",vo);
	}
	public int updateSubMenuOrder(SubMenuVO vo) {
		return this.update("subMenuDAO.updateSubMenuOrder",vo);
	}
}
