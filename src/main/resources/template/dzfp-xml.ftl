<?xml version="1.0" encoding="gbk"?>
<business id="FPKJ" comment="发票开具">
    <REQUEST_COMMON_FPKJ class="REQUEST_COMMON_FPKJ">
        <COMMON_FPKJ_FPT class="COMMON_FPKJ_FPT">
            <FPQQLSH>${kpls.kplsh?string('###')}</FPQQLSH>
            <KPLX>${kplx!"0"}</KPLX>
            <BMB_BBH>${spbmbbh!"12.0"}</BMB_BBH>
            <ZSFS>${kpls.zsfs}</ZSFS>
            <XSF_NSRSBH>${kpls.xfsh}</XSF_NSRSBH>
            <XSF_MC>${kpls.xfmc}</XSF_MC>
            <XSF_DZDH>${kpls.xfdz!}　${kpls.xfdh!}</XSF_DZDH>
            <XSF_YHZH>${kpls.xfyh!}　${kpls.xfyhzh!}</XSF_YHZH>
            <GMF_NSRSBH>${kpls.gfsh!}</GMF_NSRSBH>
            <GMF_MC>${kpls.gfmc}</GMF_MC>
            <GMF_DZDH>${gfdzdh!}</GMF_DZDH>
            <GMF_YHZH>${gfyhzh!}</GMF_YHZH>
            <KPR>${kpls.kpr!}</KPR>
            <SKR>${kpls.skr!}</SKR>
            <FHR>${kpls.fhr!}</FHR>
            <YFP_DM>${kpls.hzyfpdm!}</YFP_DM>
            <YFP_HM>${kpls.hzyfphm!}</YFP_HM>
            <JSHJ>${(kpls.jshj?string('#.######'))!}</JSHJ>
            <HJJE>${(kpls.hjje!?string('#.######'))!}</HJJE>
            <HJSE>${(kpls.hjse!?string('#.######'))!}</HJSE>
            <KCE></KCE>
            <BZ>${kpls.bz!}</BZ>
        </COMMON_FPKJ_FPT>
        <COMMON_FPKJ_XMXXS class="COMMON_FPKJ_XMXX" size="${mxCount!1}">
          <#list kpspmxList as kpspmx>
            <COMMON_FPKJ_XMXX>
                    <FPHXZ>${kpspmx.fphxz!0}</FPHXZ>
                    <SPBM>${kpspmx.spdm}</SPBM>
                    <ZXBM>${kpspmx.zxbm!}</ZXBM>
                    <YHZCBS>${kpspmx.yhzcbs!0}</YHZCBS>
                    <LSLBS>${kpspmx.lslbz!}</LSLBS>
                    <ZZSTSGL>${kpspmx.yhzcmc!}</ZZSTSGL>
                    <XMMC>${kpspmx.spmc!}</XMMC>
                    <GGXH>${kpspmx.spggxh!}</GGXH>
                    <DW>${kpspmx.spdw!}</DW>
                    <XMSL>${(kpspmx.sps?string('#.###############'))!}</XMSL>
                    <XMDJ>${(kpspmx.spdj?string('#.###############'))!}</XMDJ>
                    <XMJE>${kpspmx.spje?string('#.######')}</XMJE>
                    <SL>${kpspmx.spsl?string('#.######')}</SL>
                    <SE>${kpspmx.spse?string('#.######')}</SE>
            </COMMON_FPKJ_XMXX>
          </#list>
        </COMMON_FPKJ_XMXXS>
    </REQUEST_COMMON_FPKJ>
</business>
