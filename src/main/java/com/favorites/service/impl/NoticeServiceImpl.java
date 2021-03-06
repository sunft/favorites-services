package com.favorites.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.favorites.comm.utils.DateUtils;
import com.favorites.domain.CollectSummary;
import com.favorites.domain.CollectView;
import com.favorites.domain.CommentRepository;
import com.favorites.domain.CommentView;
import com.favorites.domain.Notice;
import com.favorites.domain.NoticeRepository;
import com.favorites.domain.PraiseRepository;
import com.favorites.domain.User;
import com.favorites.domain.UserRepository;
import com.favorites.service.NoticeService;

@Service("noticeService")
public class NoticeServiceImpl implements NoticeService{
	
	@Autowired
	private NoticeRepository noticeRepository;
	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private PraiseRepository praiseRepository;
	@Autowired
	private UserRepository userRepository;
	
	/**
	 * 保存消息通知
	 * @param collectId
	 * @param type
	 * @param userId
	 * @param operId
	 */
	public void saveNotice(String collectId,String type,Long userId,String operId){
		Notice notice = new Notice();
		notice.setCollectId(collectId);
		notice.setReaded("unread");
		notice.setType(type);
		if(StringUtils.isNotBlank(operId)){
			notice.setOperId(operId);
		}
		notice.setUserId(userId);
		notice.setCreateTime(DateUtils.getCurrentTime());
		noticeRepository.save(notice);
	}
	
	/**
	 * 展示消息通知
	 * @param type
	 * @param userId
	 * @param pageable
	 */
	@Override
	public List<CollectSummary> getNoticeCollects(String type, Long userId, Pageable pageable) {
		// TODO Auto-generated method stub
		Page<CollectView> views = noticeRepository.findViewByUserIdAndType(userId, type, pageable);
		return convertCollect(views, type);
	}

	private List<CollectSummary> convertCollect(Page<CollectView> views, String type) {
		List<CollectSummary> summarys=new ArrayList<CollectSummary>();
		for (CollectView view : views) {
			CollectSummary summary=new CollectSummary(view);
			if("at".equals(type)){
				summary.setCollectTime(DateUtils.getTimeFormatText(view.getLastModifyTime())+" at了你");
			}else if("comment".equals(type)){
				CommentView comment = commentRepository.findReplyUser(Long.valueOf(view.getOperId()));
				if(comment == null){
					continue;
				}
				summary.setUserId(comment.getUserId());
				summary.setUserName(comment.getUserName());
				summary.setProfilePicture(comment.getProfilePicture());
				if(comment.getReplyUserId() != null && comment.getReplyUserId() != 0L){
					User replyUser = userRepository.findOne(comment.getReplyUserId());
				    summary.setRemark("回复@"+replyUser.getUserName()+": "+comment.getContent());
				}else{
					summary.setRemark(comment.getContent());
				}
				summary.setCollectTime(DateUtils.getTimeFormatText(comment.getCreateTime()));
			}else if("praise".equals(type)){
				CommentView comment = praiseRepository.findPraiseUser(Long.valueOf(view.getOperId()));
				if(comment == null){
					continue;
				}
				summary.setUserId(comment.getUserId());
				summary.setUserName(comment.getUserName());
				summary.setProfilePicture(comment.getProfilePicture());
				summary.setCollectTime(DateUtils.getTimeFormatText(comment.getCreateTime())+" 赞了你的收藏");
			}		
			summarys.add(summary);
		}
		return summarys;
	}
	
	/**
	 * 获取未读消息数量
	 * @param userId
	 * @param type
	 * @return
	 */
	@Override
	public Long unreadNoticeCounts(Long userId,String type){
		if("at".equals(type)){
			return noticeRepository.countByUserIdAndTypeAndReaded(userId, "at", "unread");
		}else if("comment".equals(type)){
			return noticeRepository.countByUserIdAndTypeAndReaded(userId, "comment", "unread");
		}else if("praise".equals(type)){
			return noticeRepository.countPraiseByUserIdAndReaded(userId, "unread");
		}else{
			return 0L;
		}
	}

}
