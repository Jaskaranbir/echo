/*
 * Copyright 2018 xiaohongshu, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.echo.bearychat

import com.netflix.spinnaker.echo.api.Notification
import com.netflix.spinnaker.echo.controller.EchoResponse
import com.netflix.spinnaker.echo.notification.NotificationService
import com.netflix.spinnaker.echo.notification.NotificationTemplateEngine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty('bearychat.enabled')
class BearychatNotificationService implements NotificationService {
  private static Notification.Type TYPE = Notification.Type.BEARYCHAT

  @Autowired
  BearychatService bearychatService

  @Autowired
  NotificationTemplateEngine notificationTemplateEngine

  @Value('${bearychat.token}')
  String token

  @Override
  boolean supportsType(Notification.Type type) {
    return type == TYPE
  }

  @Override
  EchoResponse.Void handle(Notification notification) {
    //TODO: add body templates
    def body = notificationTemplateEngine.build(notification, NotificationTemplateEngine.Type.BODY)
    List<BearychatUserInfo> userList = bearychatService.getUserList(token)
    notification.to.each {
      String userid = getUseridByEmail(userList, it)
      CreateP2PChannelResponse channelInfo = bearychatService.createp2pchannel(token, new CreateP2PChannelPara(user_id: userid))
      String channelId = getVChannelid(channelInfo)
      //TODO:add text msg
      bearychatService.sendMessage(token, new SendMessagePara(vchannel_id: channelId,
        text: body,
        attachments: " " ))
    }
  }

  private static String getUseridByEmail(List<BearychatUserInfo> userList,String targetEmail) {
    return userList.find {it.email == targetEmail}.id
  }

  private static String getVChannelid(CreateP2PChannelResponse p2PChannelInfo) {
    return p2PChannelInfo.vchannel_id
  }

}
