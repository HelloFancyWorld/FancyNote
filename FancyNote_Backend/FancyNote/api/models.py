from django.contrib.auth.models import User
from django.db import models

# Create your models here.
import os
import time


def user_avatar_path(instance, filename):
    # Get the file extension
    ext = filename.split('.')[-1]
    # Generate the new filename with timestamp
    filename = f'{instance.user.username}_{int(time.time())}.{ext}'
    # Return the full path to upload the file
    return os.path.join('avatars/', filename)


class User_info(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    avatar = models.ImageField(
        upload_to=user_avatar_path, max_length=100, blank=True, null=True,)
    motto = models.CharField(max_length=100, default='这个人很懒，什么都没有留下')
    created_at = models.DateTimeField(auto_now_add=True)


class User_note(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
