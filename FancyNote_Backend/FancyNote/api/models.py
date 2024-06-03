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


def image_path(instance, filename):
    # Get the file extension
    ext = filename.split('.')[-1]
    # Generate the new filename with timestamp
    filename = f'{instance.id}_{int(time.time())}.{ext}'
    # Return the full path to upload the file
    return os.path.join('image/', filename)


def audio_path(instance, filename):
    # Get the file extension
    ext = filename.split('.')[-1]
    # Generate the new filename with timestamp
    filename = f'{instance.id}_{int(time.time())}.{ext}'
    # Return the full path to upload the file
    return os.path.join('audio/', filename)


class User_info(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    nickname = models.CharField(
        max_length=30, default='', blank=True, null=True)
    avatar = models.ImageField(
        upload_to=user_avatar_path, max_length=100, blank=True, null=True)
    motto = models.CharField(
        max_length=100, default='这个人很懒，什么都没有留下', blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)


class User_note(models.Model):
    id = models.AutoField(primary_key=True)
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    title = models.CharField(max_length=255)
    created_at = models.DateTimeField()
    updated_at = models.DateTimeField()


class Content(models.Model):
    note = models.ForeignKey(
        User_note, related_name='contents', on_delete=models.CASCADE)
    type = models.CharField(max_length=50)


class TextContent(models.Model):
    content = models.OneToOneField(
        Content, related_name='text_content', on_delete=models.CASCADE)
    text = models.TextField()


class ImageContent(models.Model):
    content = models.OneToOneField(
        Content, related_name='image_content', on_delete=models.CASCADE)
    image = models.ImageField(upload_to=image_path)
    local_path = models.CharField(max_length=255, blank=True, null=True)


class AudioContent(models.Model):
    content = models.OneToOneField(
        Content, related_name='audio_content', on_delete=models.CASCADE)
    audio = models.FileField(upload_to=audio_path)
    local_path = models.CharField(max_length=255, blank=True, null=True)
