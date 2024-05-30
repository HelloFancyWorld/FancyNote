from rest_framework import serializers
from .models import User_note, Content, TextContent, ImageContent, AudioContent


class TextContentSerializer(serializers.ModelSerializer):
    class Meta:
        model = TextContent
        fields = ['text']


class ImageContentSerializer(serializers.ModelSerializer):
    class Meta:
        model = ImageContent
        fields = ['image']


class AudioContentSerializer(serializers.ModelSerializer):
    class Meta:
        model = AudioContent
        fields = ['audio']


class ContentSerializer(serializers.ModelSerializer):
    text_content = TextContentSerializer(required=False, allow_null=True)
    image_content = ImageContentSerializer(required=False, allow_null=True)
    audio_content = AudioContentSerializer(required=False, allow_null=True)

    class Meta:
        model = Content
        fields = ['id', 'order', 'content_type',
                  'text_content', 'image_content', 'audio_content']
        extra_kwargs = {'id': {'read_only': False, 'required': False}}
        # 关键改动：将id字段设置为非只读，以便在更新内容时传递内容的ID
        # 否则更新时即使request包含内容的ID，也会被序列化器忽略


class UserNoteSerializer(serializers.ModelSerializer):
    contents = ContentSerializer(many=True, required=False)

    class Meta:
        model = User_note
        fields = ['id', 'title',
                  'created_at', 'updated_at', 'contents']

    def create(self, validated_data):
        contents_data = validated_data.pop('contents', [])
        user_note = User_note.objects.create(**validated_data)

        for content_data in contents_data:
            text_content_data = content_data.pop('text_content', None)
            image_content_data = content_data.pop('image_content', None)
            audio_content_data = content_data.pop('audio_content', None)

            content = Content.objects.create(note=user_note, **content_data)

            if text_content_data:
                TextContent.objects.create(
                    content=content, **text_content_data)

            if image_content_data:
                ImageContent.objects.create(
                    content=content, **image_content_data)

            if audio_content_data:
                AudioContent.objects.create(
                    content=content, **audio_content_data)

        return user_note

    # def update(self, instance, validated_data):
    #     contents_data = validated_data.pop('contents', None)
    #     instance.title = validated_data.get('title', instance.title)
    #     instance.save()

    #     if contents_data is not None:
    #         existing_content_ids = set(
    #             instance.contents.values_list('id', flat=True))

    #         for content_data in contents_data:
    #             content_id = content_data.get('id', None)

    #             # 如果提供了内容的ID并且存在于当前用户笔记中，则更新该内容
    #             if content_id and content_id in existing_content_ids:
    #                 content = Content.objects.get(id=content_id, note=instance)
    #                 text_content_data = content_data.pop('text_content', None)
    #                 image_content_data = content_data.pop(
    #                     'image_content', None)
    #                 audio_content_data = content_data.pop(
    #                     'audio_content', None)

    #                 for key, value in content_data.items():
    #                     setattr(content, key, value)
    #                 content.save()

    #                 if text_content_data:
    #                     text_content, _ = TextContent.objects.update_or_create(
    #                         content=content, defaults=text_content_data)

    #                 if image_content_data:
    #                     image_content, _ = ImageContent.objects.update_or_create(
    #                         content=content, defaults=image_content_data)

    #                 if audio_content_data:
    #                     audio_content, _ = AudioContent.objects.update_or_create(
    #                         content=content, defaults=audio_content_data)

    #             # 如果没有提供内容的ID或者提供的ID不存在于当前用户笔记中，则创建新内容
    #             else:
    #                 text_content_data = content_data.pop('text_content', None)
    #                 image_content_data = content_data.pop(
    #                     'image_content', None)
    #                 audio_content_data = content_data.pop(
    #                     'audio_content', None)

    #                 content = Content.objects.create(
    #                     note=instance, **content_data)

    #                 if text_content_data:
    #                     TextContent.objects.create(
    #                         content=content, **text_content_data)

    #                 if image_content_data:
    #                     ImageContent.objects.create(
    #                         content=content, **image_content_data)

    #                 if audio_content_data:
    #                     AudioContent.objects.create(
    #                         content=content, **audio_content_data)

    #     return instance

    def update(self, instance, validated_data):
        contents_data = validated_data.pop('contents', None)
        instance.title = validated_data.get('title', instance.title)
        instance.save()

        if contents_data is not None:
            existing_content_ids = set(
                instance.contents.values_list('id', flat=True))
            # print("existing_content_ids: ", existing_content_ids)

            for content_data in contents_data:
                content_id = content_data.get('id', None)
                # print("content_data: ", content_data)
                # print("content_id: ", content_id)

                if content_id and content_id in existing_content_ids:
                    # print("content_id in existing_content_ids")
                    content = Content.objects.get(id=content_id, note=instance)
                    text_content_data = content_data.pop('text_content', None)
                    image_content_data = content_data.pop(
                        'image_content', None)
                    audio_content_data = content_data.pop(
                        'audio_content', None)

                    for key, value in content_data.items():
                        setattr(content, key, value)
                    content.save()

                    if text_content_data:
                        TextContent.objects.update_or_create(
                            content=content, defaults=text_content_data)

                    if image_content_data:
                        ImageContent.objects.update_or_create(
                            content=content, defaults=image_content_data)

                    if audio_content_data:
                        AudioContent.objects.update_or_create(
                            content=content, defaults=audio_content_data)

                else:
                    # print("content_id not in existing_content_ids")
                    text_content_data = content_data.pop('text_content', None)
                    image_content_data = content_data.pop(
                        'image_content', None)
                    audio_content_data = content_data.pop(
                        'audio_content', None)

                    content = Content.objects.create(
                        note=instance, **content_data)

                    if text_content_data:
                        TextContent.objects.create(
                            content=content, **text_content_data)

                    if image_content_data:
                        ImageContent.objects.create(
                            content=content, **image_content_data)

                    if audio_content_data:
                        AudioContent.objects.create(
                            content=content, **audio_content_data)

        return instance
