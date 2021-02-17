package com.havetree.fastsql.test.controller;


import com.zto.ts.esbuff.api.anno.EsBuffField;
import com.zto.ts.esbuff.api.anno.EsBuffIndex;

import java.math.BigDecimal;
import java.util.Date;

@EsBuffIndex("maintain_repair_detail")
public class TestTable
{
    private Long aId;
    private Long bId;
    private String type;
    private String carServiceOrderCode;
    private String maintainWorkOrderCode;
    private String truckNumber;
    private  String carBrand;
    private String applyTeam;
    private String truckTeam;
    private Date startTime;
    private Date endTime;
    private String maintainPlant;

    // 项目名称
    private String projectName;
    //工时数（小时）
    private BigDecimal workHourAmount;

    // 配件名称
    private String materialName;
    // 配件数量
    private Double materialCount;

    private String remark;
    private String createUser;
    private Date createTime;

    @EsBuffField(table = "zto_maintain_work_order", id="a_id")
    private String ownerCompany;

    public Long getaId() {
        return aId;
    }

    public void setaId(Long aId) {
        this.aId = aId;
    }

    public Long getbId() {
        return bId;
    }

    public void setbId(Long bId) {
        this.bId = bId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCarServiceOrderCode() {
        return carServiceOrderCode;
    }

    public void setCarServiceOrderCode(String carServiceOrderCode) {
        this.carServiceOrderCode = carServiceOrderCode;
    }

    public String getMaintainWorkOrderCode() {
        return maintainWorkOrderCode;
    }

    public void setMaintainWorkOrderCode(String maintainWorkOrderCode) {
        this.maintainWorkOrderCode = maintainWorkOrderCode;
    }

    public String getTruckNumber() {
        return truckNumber;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }

    public String getApplyTeam() {
        return applyTeam;
    }

    public void setApplyTeam(String applyTeam) {
        this.applyTeam = applyTeam;
    }

    public String getTruckTeam() {
        return truckTeam;
    }

    public void setTruckTeam(String truckTeam) {
        this.truckTeam = truckTeam;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getMaintainPlant() {
        return maintainPlant;
    }

    public void setMaintainPlant(String maintainPlant) {
        this.maintainPlant = maintainPlant;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public Double getMaterialCount() {
        return materialCount;
    }

    public void setMaterialCount(Double materialCount) {
        this.materialCount = materialCount;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public BigDecimal getWorkHourAmount() {
        return workHourAmount;
    }

    public void setWorkHourAmount(BigDecimal workHourAmount) {
        this.workHourAmount = workHourAmount;
    }

    public String getOwnerCompany() {
        return ownerCompany;
    }

    public void setOwnerCompany(String ownerCompany) {
        this.ownerCompany = ownerCompany;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
