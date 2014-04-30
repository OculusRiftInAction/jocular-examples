package org.saintandreas.vr;

import org.saintandreas.resources.Resource;

public enum FooResource implements Resource {
  IMAGES_ICONS_BOOK_PNG("images/icons/book.png"), 
  IMAGES_ICONS_CAMERA_PNG("images/icons/camera.png"), 
  IMAGES_ICONS_CHAT_PNG("images/icons/chat.png"), 
  IMAGES_ICONS_CLOUD_PNG("images/icons/cloud.png"), 
  IMAGES_ICONS_EMAIL_PNG("images/icons/email.png"), 
  IMAGES_ICONS_FACEBOOK_PNG("images/icons/facebook.png"), 
  IMAGES_ICONS_FAVORITE_PNG("images/icons/favorite.png"),
  IMAGES_ICONS_FILE_PNG("images/icons/file.png"), 
  IMAGES_ICONS_HOME_PNG("images/icons/home.png"), 
  IMAGES_ICONS_PLAY_PNG("images/icons/play.png"), 
  IMAGES_ICONS_SETTINGS_PNG("images/icons/settings.png"), 
  IMAGES_ICONS_VIEW_PNG("images/icons/view.png"), 
  IMAGES_SKY_TRON_XNEG_PNG("images/sky/tron/xneg.png"), 
  IMAGES_SKY_TRON_XPOS_PNG("images/sky/tron/xpos.png"), 
  IMAGES_SKY_TRON_YNEG_PNG("images/sky/tron/yneg.png"), 
  IMAGES_SKY_TRON_YPOS_PNG("images/sky/tron/ypos.png"), 
  IMAGES_SKY_TRON_ZNEG_PNG("images/sky/tron/zneg.png"), 
  IMAGES_SKY_TRON_ZPOS_PNG("images/sky/tron/zpos.png"), 
  NO_RESOURCE("");

  public final String path;

  FooResource(String path) {
    this.path = path;
  }

  @Override
  public String getPath() {
    return path;
  }
}
