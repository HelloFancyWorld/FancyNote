from django.shortcuts import render

# Create your views here.

from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.decorators import login_required
from django.http import JsonResponse
from django.contrib.auth.models import User
import json
from django.middleware.csrf import get_token, rotate_token
from django.views.decorators.csrf import csrf_exempt
from .serializers import UserNoteSerializer
from .models import User_note

from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status

from api.models import User_info


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
                    'username': user.username,
                    'email': user.email,
                    'avatar_url': (user.user_info.avatar.url if user.user_info.avatar else None),
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
            user_info.avatar = file
            user_info.save()

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
