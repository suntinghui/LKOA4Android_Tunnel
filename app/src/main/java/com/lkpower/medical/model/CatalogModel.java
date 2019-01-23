package com.lkpower.medical.model;

public class CatalogModel {

	private int catalogId; //id
	private String title; // 菜单名称
	private int parentId; // 父菜单id
	private boolean transferCatalog; // 是否是交易菜单
	private String action; // 行为
	private String showBadge; // 要显示的右上角的数量
	private int iconId; // 菜单对应的图片id

	public int getCatalogId() {
		return catalogId;
	}

	public void setCatalogId(int catalogId) {
		this.catalogId = catalogId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public boolean isTransferCatalog() {
		return transferCatalog;
	}

	public void setTransferCatalog(boolean transferCatalog) {
		this.transferCatalog = transferCatalog;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getShowBadge() {
		return showBadge;
	}

	public void setShowBadge(String showBadge) {
		this.showBadge = showBadge;
	}

	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

	public String toString() {
		return this.title;
	}

}
