from django.shortcuts import render
import os

# Create your views here.

from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.decorators import login_required
from django.http import JsonResponse
from django.contrib.auth.models import User
import json
from django.middleware.csrf import get_token, rotate_token
from django.views.decorators.csrf import csrf_exempt
from .serializers import UserNoteSerializer, ImageContentSerializer, AudioContentSerializer, UserFolderSerializer
from .models import User_note, Content, ImageContent, AudioContent, User_folder, User_info

from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework.views import APIView

from api.models import User_info

import logging

logger = logging.getLogger(__name__)


@csrf_exempt
def get_csrf_token(request):
    if request.method == 'GET':
        try:
            # Rotate the CSRF token to ensure it is fresh and set it in the response
            rotate_token(request)
            csrf_token = get_token(request)
            response = JsonResponse({'csrfToken': csrf_token})
            response.set_cookie('csrftoken', csrf_token)
            return response
        except json.JSONDecodeError:
            return JsonResponse({'message': 'Invalid JSON'}, status=400)
    return JsonResponse({'message': 'Only GET requests are allowed'}, status=405)


def log_in(request):
    if request.method == 'POST':
        try:
            data = json.loads(request.body)
            username = data.get('username')
            password = data.get('password')

            user = authenticate(request, username=username, password=password)
            if user is not None:
                login(request, user)
                response = JsonResponse({
                    'message': 'Login successful',
                    'success': True,
                    'nickname': user.user_info.nickname,
                    'email': user.email,
                    'avatar': (user.user_info.avatar.url if user.user_info.avatar else None),
                    'notes': [{'id': note.id, 'title': note.title, 'updated_at': note.updated_at} for note in user.user_note_set.all()],
                    'motto': user.user_info.motto
                }, status=200)
                return response
            else:
                return JsonResponse({'message': 'Invalid credentials', 'success': False}, status=401)
        except json.JSONDecodeError:
            return JsonResponse({'message': 'Invalid JSON', 'success': False}, status=400)
    return JsonResponse({'message': 'Only POST requests are allowed', 'success': False}, status=405)


@login_required
def log_out(request):
    if request.method == 'POST':
        try:
            logout(request)
            return JsonResponse({'message': 'Logout successful', 'success': True}, status=200)
        except AttributeError:
            return JsonResponse({'message': 'You are not logged in', 'success': False}, status=400)
    return JsonResponse({'message': 'Only POST requests are allowed', 'success': False}, status=405)


def sign_up(request):
    if request.method == 'POST':
        try:
            data = json.loads(request.body)
            username = data.get('username')
            password = data.get('password')
            email = data.get('email')
            if not username or not password:
                return JsonResponse({'message': 'Missing username or password', 'success': False}, status=400)

            if User.objects.filter(username=username).exists():
                return JsonResponse({'message': 'Username already exists', 'success': False}, status=400)

            user = User.objects.create_user(
                username=username, password=password, email=email)
            user_info = User_info.objects.create(user=user)
            return JsonResponse({'message': 'User created successfully', 'success': True}, status=201)
        except json.JSONDecodeError:
            return JsonResponse({'message': 'Invalid JSON', 'success': False}, status=400)
    return JsonResponse({'message': 'Only POST requests are allowed', 'success': False}, status=405)


@login_required
def change_avatar(request):
    if request.method == 'POST':
        try:
            file = request.FILES['avatar']
            user_info = request.user.user_info

            # 获取旧的头像路径
            old_avatar_path = user_info.avatar.path if user_info.avatar else None

            user_info.avatar = file
            user_info.save()

            # 删除旧的头像文件
            if old_avatar_path and os.path.exists(old_avatar_path):
                os.remove(old_avatar_path)

            # 获取新的文件路径
            new_avatar_url = user_info.avatar.url

            return JsonResponse({'message': 'Avatar updated successfully', 'success': True, 'new_avatar_url': new_avatar_url}, status=200)
        except KeyError:
            return JsonResponse({'message': 'No file provided', 'success': False}, status=400)
        except Exception as e:
            return JsonResponse({'message': f'An error occurred: {str(e)}', 'success': False}, status=500)
    return JsonResponse({'message': 'Only POST requests are allowed', 'success': False}, status=405)


class UserNoteViewSet(viewsets.ModelViewSet):
    queryset = User_note.objects.all()
    serializer_class = UserNoteSerializer
    permission_classes = [IsAuthenticated]

    def perform_create(self, serializer):
        # 手动设置'user'字段为当前请求的用户
        serializer.save(user=self.request.user)

    def get_queryset(self):
        # 过滤查询集以仅包含当前请求用户的笔记
        return User_note.objects.filter(user=self.request.user)


class UserFolderViewSet(viewsets.ModelViewSet):
    queryset = User_folder.objects.all()
    serializer_class = UserFolderSerializer
    permission_classes = [IsAuthenticated]

    def perform_create(self, serializer):
        # 手动设置'user'字段为当前请求的用户
        serializer.save(user=self.request.user)

    def get_queryset(self):
        # 过滤查询集以仅包含当前请求用户的文件夹
        return User_folder.objects.filter(user=self.request.user)


@login_required
def edit_info(request):
    if request.method == 'POST':
        try:
            data = json.loads(request.body)
            user = request.user
            user_info = user.user_info
            user_info.nickname = data.get('nickname', user_info.nickname)
            user_info.motto = data.get('motto', user_info.motto)
            user_info.save()

            user.email = data.get('email', user.email)
            user.save()

            return JsonResponse({'message': 'User info updated successfully', 'success': True}, status=200)
        except json.JSONDecodeError:
            return JsonResponse({'message': 'Invalid JSON', 'success': False}, status=400)
    return JsonResponse({'message': 'Only POST requests are allowed', 'success': False}, status=405)


class ContentUploadView(APIView):
    def post(self, request, *args, **kwargs):
        content_id = request.data.get('content_id')
        content_type = request.data.get('type')
        file = request.FILES.get('file')

        if not content_id or not content_type or not file:
            return Response({"success": False, "message": "Missing id, type, or file"}, status=status.HTTP_400_BAD_REQUEST)

        try:
            content_instance = Content.objects.get(id=content_id)
        except Content.DoesNotExist:
            return Response({"success": False, "message": "Content not found"}, status=status.HTTP_404_NOT_FOUND)

        if content_type == '1':  # ImageContent
            image_content = ImageContent.objects.get(content=content_instance)
            image_content.image = file
            image_content.save()
            new_url = image_content.image.url
        elif content_type == '2':  # AudioContent
            audio_content = AudioContent.objects.get(content=content_instance)
            audio_content.audio = file
            audio_content.save()
            new_url = audio_content.audio.url
        else:
            return Response({"success": False, "message": "Invalid content type"}, status=status.HTTP_400_BAD_REQUEST)

        return Response({"success": True, "message": "Invalid content type", "new_file_url": new_url}, status=status.HTTP_200_OK)
