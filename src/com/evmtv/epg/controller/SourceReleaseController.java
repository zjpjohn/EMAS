package com.evmtv.epg.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.evmtv.epg.utils.StringUtils;

import org.springframework.web.bind.annotation.RequestMapping;

import com.evmtv.epg.entity.TAdv;
import com.evmtv.epg.entity.TBranch;
import com.evmtv.epg.entity.TChannels;
import com.evmtv.epg.entity.TDbConfig;
import com.evmtv.epg.entity.TMenuUsergroup;
import com.evmtv.epg.entity.TNode;
import com.evmtv.epg.entity.TNodeStatus;
import com.evmtv.epg.entity.TReleaseVersion;
import com.evmtv.epg.entity.TSource;
import com.evmtv.epg.entity.TStatusCarOrRv;
import com.evmtv.epg.entity.TTimePeriod;
import com.evmtv.epg.entity.TUser;
import com.evmtv.epg.entity.TVersionSource;
import com.evmtv.epg.release.AdvDao;
import com.evmtv.epg.release.ChannelDao;
import com.evmtv.epg.release.SourceDao;
import com.evmtv.epg.release.SqlProperty;
import com.evmtv.epg.release.TimeDao;
import com.evmtv.epg.release.VersionDao;
import com.evmtv.epg.request.SelectNode;
import com.evmtv.epg.request.Status;
import com.evmtv.epg.response.BranchVersionSourceResponse;
import com.evmtv.epg.response.PageUtils;
import com.evmtv.epg.service.IAdv;
import com.evmtv.epg.service.IBranch;
import com.evmtv.epg.service.IChannels;
import com.evmtv.epg.service.IContractAdvRescource;
import com.evmtv.epg.service.IDbConfig;
import com.evmtv.epg.service.IMenuUsergroup;
import com.evmtv.epg.service.INode;
import com.evmtv.epg.service.INodeStatus;
import com.evmtv.epg.service.IReleaseVersion;
import com.evmtv.epg.service.ISource;
import com.evmtv.epg.service.IStatusCarOrRv;
import com.evmtv.epg.service.ITimePeriod;
import com.evmtv.epg.service.IVersionAdv;
import com.evmtv.epg.service.IVersionSource;
import com.evmtv.epg.utils.ArraysUtils;
import com.evmtv.epg.utils.CollectionUtills;
import com.evmtv.epg.utils.DateUtils;
import com.evmtv.epg.utils.FileUtils;
import com.evmtv.epg.utils.UserSession;
import com.google.gson.Gson;

/***
 * 广告发布
 * TODO
 * @author fangzhu(fangzhu@evmtv.com)
 * @enclosing_type 
 * @project_name EAMS
 * @file_name AdvReleaseController.java
 * @package_name com.evmtv.epg.controller
 * @date_time 2013-7-19下午2:45:05
 * @type_name AdvReleaseController
 */
@Controller
@RequestMapping("/main/sourceRelease")
public class SourceReleaseController {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Resource IAdv iAdv;
	@Resource INode iNode;
	@Resource IBranch iBranch;
	@Resource ISource iSource;
	@Resource IDbConfig iDbConfig;
	@Resource IChannels iChannels;
	@Resource INodeStatus iNodeStatus;
	@Resource ITimePeriod iTimePeriod;
	@Resource IVersionAdv iVersionAdv;
	@Resource IStatusCarOrRv iStatusCarOrRv;
	@Resource IMenuUsergroup iMenuUsergroup;
	@Resource IVersionSource iVersionSource;
	@Resource IReleaseVersion iReleaseVersion;
	@Resource IContractAdvRescource iContractAdvRescource;
	
	/**
	 * 待发布页面
	 * @return
	 */
	@RequestMapping("/listIndex")
	public String releaseIndex(Long fmenuid,HttpServletRequest request,Model model){
		TUser user = UserSession.loginUser(request);
		//权限判断
		TMenuUsergroup mug = UserSession.getUserMenuFreadonly(user, iMenuUsergroup, fmenuid);
		model.addAttribute("usermenu", mug);
		//版本号
		List<TReleaseVersion> rvs = iReleaseVersion.selectByBranchid(user.getFbranchid(), "HD",0);
		
		model.addAttribute("rvs", rvs);
		//所有分公司
		List<TBranch> branchs = iBranch.query(new TBranch());
		model.addAttribute("branchs", branchs);
		return PageUtils.advRelease;
	}

	@RequestMapping("/sourceUsingIndex")
	public String index(Long fmenuid,Model model){
		//所有分公司
		List<TBranch> branchs = iBranch.query(new TBranch());
		model.addAttribute("branchs", branchs);
		return PageUtils.sourceUsing;
	}
	/**
	 * 获取所有分公司
	 * @param model
	 * @return
	 */
	@RequestMapping("/getBranchs")
	public String getBranchs(Model model){
		//所有分公司
		List<TBranch> branchs = iBranch.query(new TBranch());
		Gson gson = new Gson();
		model.addAttribute("result", gson.toJson(branchs));
		
		return PageUtils.json;
	}
	/**
	 * 按照版本号进行发布
	 * @param model
	 * @param vs
	 * @param request
	 * @param status 审核中的状态
	 * @return
	 */
	@RequestMapping("/release")
	public String release(Model model,TVersionSource vs, Long prervid,String status,HttpServletRequest request){
		Long rvid = vs.getFreleaseversionid();
		if(prervid == null) prervid = rvid;
		//版本信息
		TReleaseVersion rv = iReleaseVersion.selectByKey(prervid);
		BranchVersionSourceResponse bvsr = new BranchVersionSourceResponse();
		bvsr.getBranchAdv(vs, rv, iReleaseVersion, iVersionAdv);
		//该版本下 广告信息
		List<TSource> sources = iVersionSource.selectSourceJoinByVersionSource(vs);
		Status s = releaseToBranch(rv, sources, request);
		
		TUser u = UserSession.loginUser(request);
		if(s.getStatus() == 1){
			if(!rvid.toString().equals(prervid.toString())){
				rv = iReleaseVersion.selectByKey(rvid);
			}
			TReleaseVersion rvs = new TReleaseVersion();
			rvs.setId(rvid);
			if("2".equals(status)){
				rvs.setFstatus(2);
//				rvs.setFisfinishededit(1);//表示每次执行都加1
			}else{
				if(rv.getFisfinishededit() == 1){
					//获取已发布的版本号最大的版本
					TReleaseVersion v = iReleaseVersion.selectMaxIdByFbranchidAndFdefinition(rv.getFbranchid(), rv.getFdefinition(), 1);
					if(v != null)
						rvs.setFpreviousversionid(v.getId());
					//发布到省公司测试
					rvs.setFstatus(3);
				}else if(rv.getFisfinishededit() == 2){
					//发布到分公司测试
					rvs.setFstatus(4);
				}else if(rv.getFisfinishededit() == 3){
					//正式发布
					rvs.setFstatus(1);
				}
				rvs.setFisfinishededit(1);//表示每次执行都加1
			}
			rvs.setFcreateuserid(u.getId());
			rvs.setFdesc(DateUtils.getCurrentTime() + "：" + vs.getFdesc()+ "；"+vs.getFremark()+"；<br/>");
			iReleaseVersion.update(rvs);
			//分公司测试完成时不调用
			if(rv.getFisfinishededit() != 2){
				//修改版本发布状态
				updateReleaseNode(rv.getId(),u.getId(),vs.getNsid(),vs.getFremark(),rv.getFisfinishededit(),status);
			}
		}
		
		model.addAttribute("result", new Gson().toJson(s));
		
		return PageUtils.json;
	}
	/**
	 * 修改节点状态
	 * @param rvid 版本索引
	 * @param uid 用户索引
	 * @param nsid 审核节点索引
	 * @param remark 审核节点信息
	 * @param fisfinishededit 节点到达位置
	 */
	private void updateReleaseNode(Long rvid,Long uid,Long nsid,String remark,Integer fisfinishededit,String s){
		TNode node = null;
		if(nsid == null){
			if(fisfinishededit == 1){
				//发布到省公司测试
				node = SelectNode.getReleaseToProvNode();
			}else if(fisfinishededit == 3){
				//发布正式播出
				node = SelectNode.getReleaseNode();
			}
			node = iNode.selectByNode(node);
			nsid = iNodeStatus.selectByCaridOrRvidAndNodeid(null,rvid, node.getId()).getId();
		}
		//广告发布节点流程
		TNodeStatus status = new TNodeStatus();
		status.setFreleaseversionid(rvid);
		status.setFstatus(s);
		status.setFuserid(uid);
		status.setFremark(remark);
		status.setFcreatetime(DateUtils.getCurrentTime());
		status.setId(nsid);
		
		iNodeStatus.update(status);

		if(!StringUtils.hasText(s)){
			Long ugid = -1L;
			//状态
			if(fisfinishededit == 1){//发布到省公司测试
				node = SelectNode.getProveTestNode();
				node = iNode.selectByNode(node);
				ugid = node.getFusergroupid();
			}
			TStatusCarOrRv scor = new TStatusCarOrRv();
			scor.setFisvalid("1");
			scor.setFnextnodeusergroupid(ugid);
			scor.setFreleaseversionid(rvid);
			
			iStatusCarOrRv.update(scor);
		}
	}
	/**
	 * 广告发布至分公司
	 * @param rv
	 * @param bloBs
	 * @param request
	 * @return
	 */
	private Status releaseToBranch(TReleaseVersion rv, List<TSource> bloBs,HttpServletRequest request){
		//解析度
		String definition = rv.getFdefinition();
		//分公司索引
		Long fbranchid = rv.getFbranchid();
		//广告数据
		List<TSource> sources = new ArrayList<TSource>();
		
		Set<Long> timesIdSet = new HashSet<Long>(); //该公司广告时间段
		Set<Long> advsIdSet = new HashSet<Long>();//该公司广告的广告位
		List<Long> sourceId = new ArrayList<Long>();//广告索引
		//当前日期
		String cerruntDate = DateUtils.formatDate();
		String endDate = DateUtils.addYear(3, cerruntDate);
		//绝对路径
		String realPath = FileUtils.getRealPath(request);
		//状态
		Status status = new Status();
		
		for(TSource s : bloBs){
			s.setFplaydate(cerruntDate);
			s.setFenddate(endDate);
			
			if(s.getTid() != null){
				timesIdSet.add(s.getTid());
				s.setFtimeperiodid(s.getTid());
			}else{
				s.setFtimeperiodid(-1L);
			}
			if(s.getFadvid() != null)
				advsIdSet.add(s.getFadvid());
			if(s.getId() != null)
				sourceId.add(s.getId());
			
			String filePath = FileUtils.checkFilePathEndSep(realPath).concat(s.getFpath());
			String suffix = FileUtils.getSuffix(filePath);
			String t = suffix.equals("m2v") ? "3" : suffix.equals("mpg") ? "10" : "0";
			s.setFelementtype(t);
			try {
				//文件转字节数组
				s.setFbitdata(FileUtils.readFileToBytes(filePath));
			} catch (IOException e) {
				logger.info(e.getMessage());
			}
			//频道信息
			String expand = s.getFexpand();
			if(StringUtils.hasText(expand)){
				List<Long> channels = ArraysUtils.toLongList(expand);
				for(Long cid : channels){
					if(cid != null){
						TSource s1 = new TSource();
						BeanUtils.copyProperties(s, s1);
						s1.setId(null);
						s1.setFexpand(null);
						s1.setFchannelsid(cid);
						sources.add(s1);
					}
				}
			}else{
				s.setFchannelsid(-1L);
				sources.add(s);
			}
		}
		//发布到分公司
		TBranch branch = iBranch.queryById(fbranchid);
		if(CollectionUtills.hasElements(sources)){
			//发布广告
			TDbConfig db = null;
			if(rv.getFisfinishededit() > 1)
				db = iDbConfig.query(fbranchid);
			else
				db = iDbConfig.query(-1L);//发布至省公司测试环境
			if(db != null){
				//删除已有数据,追加发布时不需删除
				SourceDao dao = new SourceDao(db);
				dao.delete(SqlProperty.Delete.DELETE_SOURCE_SQL_ALL + "'"+definition + "'");
				ChannelDao cdao = new ChannelDao(db);
				cdao.delete(SqlProperty.Delete.DELETE_CHANNELS_SQL_ALL);
				TimeDao tdao = new TimeDao(db);
				tdao.delete(SqlProperty.Delete.DELETE_TIME_SQL_ALL + "'" + definition + "'");
				AdvDao advDao = new AdvDao(db);
				advDao.delete(SqlProperty.Delete.DELETE_ADV_SQL_ALL + "'" + definition + "'");
				
				//频点业务
				Long rvid = iChannels.selectMaxRvIdByBranchid(fbranchid);
				if(rvid != null){
					List<TChannels> channels = iChannels.queryByBranchId(fbranchid, rvid);//iChannels.queryByIdList(new ArrayList<Long>(channelsIdSet));
					if(CollectionUtills.hasElements(channels)){
						ChannelDao channelDao = new ChannelDao(db);
						channelDao.insert(channels);
					}
				}
				//时间段
				if(CollectionUtills.hasElements(timesIdSet)){
					List<TTimePeriod> periods = iTimePeriod.queryByIdList(new ArrayList<Long>(timesIdSet));
					if(CollectionUtills.hasElements(periods)){
						TimeDao timeDao = new TimeDao(db);
						timeDao.insert(periods, definition);
					}
				}
				if(CollectionUtills.hasElements(advsIdSet)){
					List<TAdv> advs = iAdv.select(new ArrayList<Long>(advsIdSet));
					if(CollectionUtills.hasElements(advs)){
						advDao = new AdvDao(db);
						advDao.insert(SqlProperty.Filed.advColumns, advs);
					}
				}
				//发布广告数据
				dao = new SourceDao(db);
				if(dao.insert(SqlProperty.Filed.sourceColumns, sources) > 0){
					//更新版本
					VersionDao versionDao = new VersionDao(db);
					versionDao.update();
					
					status.setResult(branch.getFname()+"广告发布成功！");
					status.setStatus(1);
				}else{
					status.setResult(branch.getFname()+"广告数据信息发布失败！");
					status.setStatus(0);
				}
			}else{
				status.setResult("暂无"+branch.getFname()+"数据库服务器相关配置信息，请联系管理员！");
				status.setStatus(0);
			}
		}else{
			status.setResult(branch.getFname()+"暂无需要发布广告数据信息！");
			status.setStatus(0);
		}
		
		return status;
	}
	/**
	 * 广告发布
	 * @param model
	 * @param sourceIds
	 * @param rv 发布版本号
	 * @param request
	 * @return
	 */
//	@RequestMapping("/release")
	/*@Deprecated
	public String release(Model model,String sourceIds,TReleaseVersion rv,HttpServletRequest request){
		boolean isDayVersion = rv.getId() == null;
		
		//广告id
		List<Long> sourceids = ArraysUtils.toLongList(sourceIds);
		//当前用户
		TUser user = UserSession.loginUser(request);
		if(isDayVersion){
			rv.setFcreatetime(DateUtils.getCurrentTime());
			rv.setFcreateuserid(user.getId());
			iReleaseVersion.insert(rv);
		}else{
			rv.setFdayversion(null);
			iReleaseVersion.update(rv);
			TReleaseVersion v = iReleaseVersion.selectMaxUpdateTimeByBranchid(rv.getFbranchid(),rv.getFdefinition());
			if(!rv.getId().toString().equals(v.getId().toString())){
				List<Long> sids = iSourceReleaseVersion.selectSourceIdByReleaseVersionId(rv.getId());
				sourceids.addAll(sids);
			}
		}
		String realPath = FileUtils.getRealPath(request);
		
		//发布广告至分公司
		String result = releaseToBranch(rv.getFbranchid(), sourceids,realPath,isDayVersion,rv.getId(),user.getId(),rv.getFdefinition());

		model.addAttribute("result", result);
		return PageUtils.json;
	}*/
	/**
	 * 发布广告至分公司
	 * @param fbranchid
	 * @param sourceids
	 * @param realPath
	 * @return
	 */
	/*@Deprecated
	private String releaseToBranch(Long fbranchid, List<Long> sourceids, String realPath,boolean isDayVersion, Long rvid,Long uid,String definition){
		//获取选中广告信息
		List<TSourceWithBLOBs> bloBs = iSource.selectByIdList(sourceids);

		//广告数据
		List<TSourceWithBLOBs> sources = new ArrayList<TSourceWithBLOBs>();
		//广告发布节点
		TNode node = getReleaseNode();
		
		Set<Long> timesIdSet = new HashSet<Long>(); //该公司广告时间段
		Set<Long> channelsIdSet = new HashSet<Long>(); //该公司广告频点
		Set<Long> advsIdSet = new HashSet<Long>();//该公司广告的广告位
		List<Long> sourceId = new ArrayList<Long>();//广告索引
		Set<Long> contractIdSet = new HashSet<Long>();//合同索引
		
		//当前日期
		String cerruntDate = DateUtils.formatDate();
		String endDate = DateUtils.addYear(3, cerruntDate);
		
		for(TSourceWithBLOBs s : bloBs){
			
			s.setFplaydate(cerruntDate);
			s.setFenddate(endDate);
			
			if(s.getFtimeperiodid() != null)
				timesIdSet.add(s.getFtimeperiodid());
			if(s.getFadvid() != null)
				advsIdSet.add(s.getFadvid());
			if(s.getId() != null)
				sourceId.add(s.getId());
			if(s.getFcontractid() != null)
				contractIdSet.add(s.getFcontractid());
			
			String filePath = FileUtils.checkFilePathEndSep(realPath).concat(s.getFpath());
			String suffix = FileUtils.getSuffix(filePath);
			String t = suffix.equals("m2v") ? "3" : suffix.equals("mpg") ? "10" : "0";
			s.setFelementtype(t);
			try {
				//文件转字节数组
				s.setFbitdata(FileUtils.readFileToBytes(filePath));
			} catch (IOException e) {
				logger.info(e.getMessage());
			}
			//频道信息
			String expand = s.getFexpand();
			if(StringUtils.hasText(expand)){
				List<Long> channels = ArraysUtils.toLongList(expand);
				for(Long cid : channels){
					if(cid != null){
						channelsIdSet.add(cid);
						TSourceWithBLOBs s1 = s;
						s1.setId(null);
						s1.setFexpand(null);
						s1.setFchannelsid(cid);
						sources.add(s1);
					}
				}
			}else{
				sources.add(s);
			}
		}
		String result = "";
		//发布到分公司
		TBranch branch = iBranch.queryById(fbranchid);
		if(CollectionUtills.hasElements(sources)){
			//发布广告
			TDbConfig db = iDbConfig.query(fbranchid);
			if(db != null){
				//删除已有数据,追加发布时不需删除
				SourceDao dao = null;
				if(isDayVersion){
					dao = new SourceDao(db);
					dao.delete();
					ChannelDao cdao = new ChannelDao(db);
					cdao.delete(SqlProperty.Delete.DELETE_CHANNELS_SQL_ALL+definition);
					TimeDao tdao = new TimeDao(db);
					tdao.delete(SqlProperty.Delete.DELETE_TIME_SQL_ALL+definition);
					AdvDao advDao = new AdvDao(db);
					advDao.delete(SqlProperty.Delete.DELETE_ADV_SQL_ALL+definition);
				}
				dao = new SourceDao(db);
				//频点业务
				if(CollectionUtills.hasElements(channelsIdSet)){
					List<TChannels> channels = iChannels.queryByIdList(new ArrayList<Long>(channelsIdSet));
					if(CollectionUtills.hasElements(channels)){
						ChannelDao channelDao = new ChannelDao(db);
						channelDao.insert(channels, definition);
					}
				}
				//时间段
				if(CollectionUtills.hasElements(timesIdSet)){
					List<TTimePeriod> periods = iTimePeriod.queryByIdList(new ArrayList<Long>(timesIdSet));
					if(CollectionUtills.hasElements(periods)){
						TimeDao timeDao = new TimeDao(db);
						timeDao.insert(periods, definition);
					}
				}
				if(CollectionUtills.hasElements(advsIdSet)){
					List<TAdv> advs = iAdv.select(new ArrayList<Long>(advsIdSet));
					if(CollectionUtills.hasElements(advs)){
						AdvDao advDao = new AdvDao(db);
						advDao.insert(SqlProperty.Filed.advColumns, advs);
					}
				}
				//发布广告数据
				if(dao.insert(SqlProperty.Filed.sourceColumns, sources) > 0){
					//更新版本
					VersionDao versionDao = new VersionDao(db);
					versionDao.update();
					//分公司发布版本
					TSourceReleaseVersion v = new TSourceReleaseVersion();
					v.setFbranchid(fbranchid);
					v.setFreleaseversionid(rvid);
					v.setSources(sourceids);
					
//					iSourceReleaseVersion.insertBatchSelective(v);
					
					//广告节点状态
					insertNodeStatus(sources,node,uid,fbranchid);
					
					result = "{'status':1,'result':'"+branch.getFname()+"广告发布成功！'}";
				}else{
					result = "{'status':0,'result':'"+branch.getFname()+"广告数据信息发布失败'}";
				}
			}else{
				result = "{'status':0,'result':'暂无"+branch.getFname()+"数据库服务器相关配置信息，请联系管理员'}";
			}
		}else{
			result = "{'status':0,'result':'"+branch.getFname()+"暂无需要发布广告数据信息'}";
		}
		
		return result;
	}*/
	/**
	 * 查询广告信息
	 * @param model
	 * @param source
	 * @return
	 *//*
	@RequestMapping("/query")
	@Deprecated
	public String query(Model model,TSource source,HttpServletRequest request){
		source.setHolder(new IntHolder());
		//广告信息
		List<TSourceWithBLOBs> sources = loadRelease(source,request);
		int total = source.getHolder().value;
		//总页数
		int totalPage = ReturnJsonUtils.getTotalPage(source.getLimit(), total);
		//返回字符串
		String result = ReturnJsonUtils.getReturnJsonStr(sources, total, totalPage, source.getPage());

		model.addAttribute("result", result);
		
		return PageUtils.json;
	}
	*//**
	 * 加载广告数据
	 * @param source
	 * @param request
	 * @return
	 *//*
	@Deprecated
	private List<TSourceWithBLOBs> loadRelease(TSource source, HttpServletRequest request) {
		if(true){
			// 当前用户
			if (source.getFbranchid() == null) {
				TUser user = UserSession.loginUser(request);
				source.setFbranchid(user.getFbranchid());
			}
			// 当前用操作节点
			TNode node = getReleaseNode();
			int order = node.getForder();// 排序
			source.setFtop(node.getId().toString());
			node.setForder(order);
			node.setFischecked(null);
			
			List<TNode> nodes = iNode.selectByExample(node);
			if(CollectionUtills.hasElements(nodes)){
				Long nodeid = nodes.get(0).getId();
				source.setNodeid(nodeid);
			}
			// 广告位
			if (source.getFadvid() == null && !CollectionUtills.hasElements(source.getIdList())) {
				// 得到分公司广告索引
				List<TAdv> advs = iAdv.query(source.getFbranchid());
				// 省公司广告位
				List<TAdv> proCompanyadvs = iAdv.query(1L);
				List<Long> advid = new ArrayList<Long>();
				List<Long> aids = source.advToIdList(advs);
				if (CollectionUtills.hasElements(aids))
					advid.addAll(aids);
				aids = source.proCompanyAdvToIdList(proCompanyadvs, advs);
				if (CollectionUtills.hasElements(aids))
					advid.addAll(aids);
				source.setAdvIdList(advid);
			}
		}
		// 广告信息
		return iSource.selectRelease(source);
	}
	
	*//**
	 * 版本查询
	 * @param model
	 * @param fbranchid
	 * @return
	 *//*
	@RequestMapping("/releaseVersion")
	public String sourceReleaseVersion(Model model, Long fbranchid,String definition,Integer status){
		//分公司已发布广告
		List<TReleaseVersion> releases = iReleaseVersion.selectByBranchid(fbranchid,definition,status);
		
		Gson gson = new Gson();
		String result = gson.toJson(releases);
		model.addAttribute("result", result);
		
		return PageUtils.json;
	}*/
	
	
	/**
	 * 分公司广告发布
	 * @param model
	 * @param fbranchid 分公司广告位
	 * @param source 广告位
	 * @param request
	 * @return
	 *//*
//	@RequestMapping("/release")
	@Deprecated
	public String release(Model model,Long fbranchid,String sourceIds,HttpServletRequest request,String version){
		if(!StringUtils.hasText(version))
			version = DateUtils.formatDate();
		// 当前用操作节点
		TNode node = getReleaseNode();
		//当前用户
		TUser user = UserSession.loginUser(request);
		//广告信息
		TSource source = new TSource();
		if(StringUtils.hasText(sourceIds))
			source.setIdList(ArraysUtils.toLongList(sourceIds));
		//广告发布
		releasing(fbranchid, loadRelease(source,request), user.getId(), version, node, FileUtils.getRealPath(request));

		model.addAttribute("result", result);
		return PageUtils.json;
	}
	
	*/
	/**
	 * 广告发布状态修改
	 * @param sources
	 * @param nodeid
	 * @param uid
	 * @return
	 *//*
	private void insertNodeStatus(List<TSourceWithBLOBs> sources,TNode node,Long uid){
		for(TSourceWithBLOBs s : sources){
			//广告发布节点流程
			TNodeStatus status = new TNodeStatus();
			status.setFcontractadvid(s.getCaid());
			status.setFcontractadvresourceid(s.getCarid());
			status.setFcontractid(s.getFcontractid());
			status.setFcreatetime(DateUtils.getCurrentTime());
			status.setFsourceid(s.getId());
			status.setFremark("成功发布");
			status.setFnodeid(node.getId());
			status.setFstatus("1");
			status.setFuserid(uid);
			
			if(!iNodeStatus.query(status)){
				iNodeStatus.insert(status);
				iContractAdvRescource.updateCheckedNodeId(s.getCarid(), node.getId(),node.getForder());
			}
		}
	}
	*//**
	 * 广告发布版本
	 * @param model
	 * @param fbranchid 分公司索引
	 * @return
	 *//*
	@RequestMapping("/releaseVersion")
	public String sourceReleaseVersion(Model model, Long fbranchid){
		//分公司已发布广告
		List<TBranchSourceRelease> releases = iBranchSourceRelease.selectByBranchid(fbranchid);
		
		Gson gson = new Gson();
		String result = gson.toJson(releases);
		model.addAttribute("result", result);
		
		return PageUtils.json;
	}
	*//**
	 * 广告发布
	 * @param model
	 * @param sourceIds
	 * @param request
	 * @return
	 *//*
	@RequestMapping("/releaseBySource")
	public String release(Model model,String sourceIds,HttpServletRequest request){
		String version = DateUtils.formatDate();
		// 当前用操作节点
		TNode node = getReleaseNode();
		//当前用户
		TUser user = UserSession.loginUser(request);
		//返回值
		List<String> resultList = new ArrayList<String>();
//		String result = "";
		//以选择待发布广告索引
		if(StringUtils.hasText(sourceIds)){
			List<Long> sourceids = ArraysUtils.toLongList(sourceIds);
			List<TSourceWithBLOBs> sources = iSource.selectByIdList(sourceids);
			
			waitRelease(sources,FileUtils.getRealPath(request),false);
			//公用广告发布
			proSourceRelease(user.getId(),version,node,resultList);
			//分公司广告
			for(Entry<Long, List<TSourceWithBLOBs>> entry : branchSource.entrySet()){
				//分公司索引
				Long branchid = entry.getKey();
				sources = entry.getValue();//广告
				//发布
				releasing(branchid, sources, user.getId(), version, node, null);
				resultList.add(result);
			}
		}
		
		model.addAttribute("result", new Gson().toJson(resultList));
		return PageUtils.json;
	}
	*//**
	 * 正在发布广告数据
	 * @param fbranchid 分公司
	 * @param sources 分公司广告数据
	 * @param uid 发布广告用户索引
	 * @param version 版本
	 * @param nid 节点索引
	 * @param realPath
	 *//*
	private void releasing(Long fbranchid, List<TSourceWithBLOBs> sources, Long uid, String version, TNode node, String realPath){
		TBranch branch = iBranch.queryById(fbranchid);
		if(CollectionUtills.hasElements(sources)){
			//发布广告
			TDbConfig db = iDbConfig.query(fbranchid);
			if(db != null){
				//删除已有数据
				SourceDao dao = new SourceDao(db);
				dao.delete();
				
				dao = new SourceDao(db);
				List<TSourceWithBLOBs> bloBs = waitRelease(sources, realPath,true);
				//频点业务
				if(CollectionUtills.hasElements(channelsIdSet)){
					List<TChannels> channels = iChannels.queryByIdList(new ArrayList<Long>(channelsIdSet));
					if(CollectionUtills.hasElements(channels)){
						ChannelDao channelDao = new ChannelDao(db);
						channelDao.insert(SqlProperty.Filed.channelColumns, channels);
					}
				}
				//时间段
				if(CollectionUtills.hasElements(timesIdSet)){
					List<TTimePeriod> periods = iTimePeriod.queryByIdList(new ArrayList<Long>(timesIdSet));
					if(CollectionUtills.hasElements(periods)){
						TimeDao timeDao = new TimeDao(db);
						timeDao.insert(SqlProperty.Filed.timeColums, periods);
					}
				}
				if(CollectionUtills.hasElements(advsIdSet)){
					List<TAdv> advs = iAdv.select(new ArrayList<Long>(advsIdSet));
					if(CollectionUtills.hasElements(advs)){
						AdvDao advDao = new AdvDao(db);
						advDao.insert(SqlProperty.Filed.advColumns, advs);
					}
				}
				//发布广告数据
				if(dao.insert(SqlProperty.Filed.sourceColumns, bloBs) > 0){
					//更新版本
					VersionDao versionDao = new VersionDao(db);
					versionDao.update();//SqlProperty.toUpdateColumns(pos)
					//已发布数据
					TBranchSourceRelease release = new TBranchSourceRelease();
					//保存已更新广告
					release.setFbranchid(fbranchid);
					release.setFcreatetime(DateUtils.getCurrentTime());
					release.setFsourceidList(sourceId);
					release.setFcontractidList(contractId);
					release.setSources(sources);
					release.setFisusing("1");
					release.setFcreateuserid(uid);
					release.setFversion(version);
					//保存已发布广告数据
					iBranchSourceRelease.insert(release);
					//广告节点状态
					insertNodeStatus(sources,node,uid);
					
					result = "{'status':1,'result':'"+branch.getFname()+"广告发布成功！'}";
				}else{
					result = "{'status':0,'result':'"+branch.getFname()+"广告数据信息发布失败'}";
				}
			}else{
				result = "{'status':0,'result':'暂无"+branch.getFname()+"数据库服务器相关配置信息，请联系管理员'}";
			}
		}else{
			result = "{'status':0,'result':'"+branch.getFname()+"暂无需要发布广告数据信息'}";
		}
	}
	*//**
	 * 广告发布节点
	 * @return
	 *//*
	private TNode getReleaseNode(){
		// 当前用操作节点
		TNode node = new TNode();
		node.setFischecked("2");
		node.setStart(0);
		node.setLimit(1);
		List<TNode> nodes = iNode.selectByExample(node);
		if(CollectionUtills.hasElements(nodes))
			node = nodes.get(0);
		return node;
	}
	
	private Set<Long> timesIdSet = null; //该公司广告时间段
	private Set<Long> channelsIdSet = null; //该公司广告频点
	private Set<Long> advsIdSet = null;//该公司广告的广告位
	private List<Long> sourceId = null;//广告索引
	private List<Long> contractId = null;//合同索引
	private boolean hasProSource = false; //是否有省公司广告
	private Set<Long> proAdvIds = null;//省公司广告位索引
	private Map<Long,List<TSourceWithBLOBs>> branchSource = null;//分公司广告为索引
	private String result = "";
	*//**
	 * 待发布数据
	 * @param sources
	 * @param realPath
	 * @param isBranchRelease 按分公司发布
	 * @return 
	 *//*
	private List<TSourceWithBLOBs> waitRelease(List<TSourceWithBLOBs> sources,String realPath,boolean isBranchRelease){
		
		timesIdSet = new HashSet<Long>(); //该公司广告时间段
		channelsIdSet = new HashSet<Long>(); //该公司广告频点
		channelsIdSet.add(-1L);
		advsIdSet = new HashSet<Long>();//该公司广告的广告位
//		pos = new HashSet<Integer>();//广告位
		sourceId = new ArrayList<Long>();//广告索引
		contractId = new ArrayList<Long>();//合同索引
		if(!isBranchRelease){
			proAdvIds = new HashSet<Long>();//省公司广告位索引
			branchSource = new HashMap<Long, List<TSourceWithBLOBs>>();//分公司广告索引
		}
		
		List<TSourceWithBLOBs> bloBs = new ArrayList<TSourceWithBLOBs>();
		for(TSourceWithBLOBs s : sources){
			//广告位索引
			if(s.getFtimeperiodid() != null)
				timesIdSet.add(s.getFtimeperiodid());
			if(s.getFadvid() != null)
				advsIdSet.add(s.getFadvid());
			if(s.getFcontractid() != null)
				contractId.add(s.getFcontractid());
			if(s.getId() != null)
				sourceId.add(s.getId());
			
			//分公司索引
			Long branchid = s.getFbranchid();
			List<TSourceWithBLOBs> bs = null;
			if(!isBranchRelease){
				bs = branchSource.get(branchid);
				if(bs == null){
					bs = new ArrayList<TSourceWithBLOBs>();
				}
				if("1".equals(branchid.toString())){
					proAdvIds.add(s.getFadvid());
					if(!hasProSource)
						hasProSource = true;
				}
			}
			if(StringUtils.hasText(realPath)){
				//真实路径
				try {
					realPath = FileUtils.checkFilePathEndSep(realPath);
					String filePath = realPath.concat(s.getFpath());
					String suffix = FileUtils.getSuffix(filePath);
					String t = suffix.equals("m2v") ? "3" : suffix.equals("mpg") ? "10" : "0";
					s.setFelementtype(t);
					s.setFbitdata(FileUtils.readFileToBytes(filePath));
				} catch (IOException e) {
					result = "{'status':0,'result':'文件转字节数组失败,找不到文件'}";
				}
			}
			
			//该广告位频点
			List<Long> channelid = ArraysUtils.toLongList(s.getFexpand());
			s.setFsourceid(s.getId());
			s.setFchannelsid(-1L);
			if(CollectionUtills.hasElements(channelid)){
				for(Long cid : channelid){
					channelsIdSet.add(cid);
					TSourceWithBLOBs currentSource = s;
					currentSource.setFchannelsid(cid);
					currentSource.setFexpand(null);
					currentSource.setId(null);

					bloBs.add(currentSource);
					if(!isBranchRelease){
						bs.add(currentSource);
					}
				}
			}else{
				s.setId(null);
				bloBs.add(s);
				if(!isBranchRelease){
					bs.add(s);
				}
			}
			if(!isBranchRelease){
				branchSource.put(branchid, bs);
			}
		}
		
		return bloBs;
	}
	*//**
	 * 公用广告发布
	 * @param uid
	 * @param version
	 * @param nid
	 *//*
	private void proSourceRelease(Long uid, String version, TNode node,List<String> resultList){
		//省公司广告位
		if(CollectionUtills.hasElements(proAdvIds)){
			List<Long> advids = new ArrayList<Long>(proAdvIds);
			List<TAdv> proAdvs = iAdv.select(advids);
			//公用广告位
			List<TSourceWithBLOBs> proSources = branchSource.get(1L);
			//所有分公司
			List<TBranch> branchs = iBranch.query(new TBranch());
			for(TBranch b : branchs){
				Long branchid = b.getId();
				if(!"1".equals(branchid.toString())){
					//当前分公司广告
					List<TSourceWithBLOBs> mySource = new ArrayList<TSourceWithBLOBs>();
					//分公司广告位
					List<TAdv> badvs = iAdv.query(branchid);
					if(CollectionUtills.hasElements(proAdvs)){
						for(TAdv pa : proAdvs){
							boolean bool = false;
							for(int i=0,len=badvs.size();i<len;i++){
								if(pa.getFpositionid() != 5 && badvs.get(i).getFpositionid() == pa.getFpositionid()){
									break;
								}
								if(i==len-1){
									bool = true;
								}
							}//添加公用部分广告
							if(bool){
								for(TSourceWithBLOBs s : proSources){
									if(s.getFadvid().toString().equals(pa.getId().toString())){
										mySource.add(s);
									}
								}
							}
						}
					}
					if(CollectionUtills.hasElements(mySource)){
						//发布数据
						mySource = waitRelease(mySource, null, true);
						//查询该公司已发布的广告，将其删除
						mySource = iBranchSourceRelease.query(mySource,branchid);
						//发布
						releasing(branchid, mySource, uid, version, node, null);
						
						resultList.add(result);
					}
				}
			}
		}
	}*/
}