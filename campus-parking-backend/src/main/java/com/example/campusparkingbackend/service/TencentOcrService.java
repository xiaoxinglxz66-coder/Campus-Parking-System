package com.example.campusparkingbackend.service;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.LicensePlateOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.LicensePlateOCRResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TencentOcrService {
    @Value("${tencent.cloud.secretId}")
    private String secretId;
    @Value("${tencent.cloud.secretKey}")
    private String secretKey;
    @Value("${tencent.cloud.ocr.region:ap-beijing}")
    private String region;

    public String recognizeLicensePlate(MultipartFile file) throws Exception {
        // 1. 图片转Base64
        byte[] bytes = file.getBytes();
        String imageBase64 = java.util.Base64.getEncoder().encodeToString(bytes);
        // 2. 初始化腾讯云客户端
        Credential cred = new Credential(secretId, secretKey);
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("ocr.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        OcrClient client = new OcrClient(cred, region, clientProfile);

        // 3. 构建并发送识别请求
        LicensePlateOCRRequest req = new LicensePlateOCRRequest();
        req.setImageBase64(imageBase64);
        LicensePlateOCRResponse resp = client.LicensePlateOCR(req);

        // 4. 返回识别出的车牌号
        return resp.getNumber();
    }
}
