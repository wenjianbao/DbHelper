package wen.jianbao.model.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseBlog<M extends BaseBlog<M>> extends Model<M> implements IBean {

	public M setBlogId(String blogId) {
		set("blogId", blogId);
		return (M)this;
	}
	
	public String getBlogId() {
		return getStr("blogId");
	}

	public M setTitle(String title) {
		set("title", title);
		return (M)this;
	}
	
	public String getTitle() {
		return getStr("title");
	}

	public M setIntro(String intro) {
		set("intro", intro);
		return (M)this;
	}
	
	public String getIntro() {
		return getStr("intro");
	}

	public M setSortId(String sortId) {
		set("sortId", sortId);
		return (M)this;
	}
	
	public String getSortId() {
		return getStr("sortId");
	}

	public M setTagIds(String tagIds) {
		set("tagIds", tagIds);
		return (M)this;
	}
	
	public String getTagIds() {
		return getStr("tagIds");
	}

	public M setStatus(Boolean status) {
		set("status", status);
		return (M)this;
	}
	
	public Boolean getStatus() {
		return get("status");
	}

	public M setPageKeywords(String pageKeywords) {
		set("pageKeywords", pageKeywords);
		return (M)this;
	}
	
	public String getPageKeywords() {
		return getStr("pageKeywords");
	}

	public M setPageDescription(String pageDescription) {
		set("pageDescription", pageDescription);
		return (M)this;
	}
	
	public String getPageDescription() {
		return getStr("pageDescription");
	}

	public M setIsTop(Boolean isTop) {
		set("isTop", isTop);
		return (M)this;
	}
	
	public Boolean getIsTop() {
		return get("isTop");
	}

	public M setViewCount(Long viewCount) {
		set("viewCount", viewCount);
		return (M)this;
	}
	
	public Long getViewCount() {
		return getLong("viewCount");
	}

	public M setAddBy(String addBy) {
		set("addBy", addBy);
		return (M)this;
	}
	
	public String getAddBy() {
		return getStr("addBy");
	}

	public M setAddTime(Long addTime) {
		set("addTime", addTime);
		return (M)this;
	}
	
	public Long getAddTime() {
		return getLong("addTime");
	}

	public M setUpdateBy(String updateBy) {
		set("updateBy", updateBy);
		return (M)this;
	}
	
	public String getUpdateBy() {
		return getStr("updateBy");
	}

	public M setUpdateTime(Long updateTime) {
		set("updateTime", updateTime);
		return (M)this;
	}
	
	public Long getUpdateTime() {
		return getLong("updateTime");
	}

}