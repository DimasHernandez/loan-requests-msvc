package co.com.pragma.consumer.user.mapper;

import co.com.pragma.consumer.user.UserBasicInfoResponse;
import co.com.pragma.consumer.user.UserInfoResponse;
import co.com.pragma.model.user.User;
import co.com.pragma.model.userbasicinfo.UserBasicInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserInfoResponse userInfo);

    UserBasicInfo toBasicInfo(UserBasicInfoResponse userBasicInfoResponse);
}
