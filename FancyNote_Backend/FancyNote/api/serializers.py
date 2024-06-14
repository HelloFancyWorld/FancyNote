from rest_framework import serializers
from .models import User_note, Content, TextContent, ImageContent, AudioContent


class TextContentSerializer(serializers.ModelSerializer):
    class Meta:
        model = TextContent
        fields = ['text']


class ImageContentSerializer(serializers.ModelSerializer):
    imageUrl = serializers.SerializerMethodField()

    class Meta:
        model = ImageContent
        fields = ['local_path', 'imageUrl']

    def get_imageUrl(self, obj):
        return obj.image.url if obj.image else None


class AudioContentSerializer(serializers.ModelSerializer):
    audioUrl = serializers.SerializerMethodField()

    class Meta:
        model = AudioContent
        fields = ['local_path', 'audioUrl']

    def get_audioUrl(self, obj):
        return obj.audio.url if obj.audio else None


class ContentSerializer(serializers.ModelSerializer):
    text_content = TextContentSerializer(required=False, allow_null=True)
    image_content = ImageContentSerializer(required=False, allow_null=True)
    audio_content = AudioContentSerializer(required=False, allow_null=True)

    content = serializers.CharField(write_only=True, allow_blank=True)
    type = serializers.IntegerField()

    class Meta:
        model = Content
        fields = ['id', 'type', 'text_content',
                  'image_content', 'audio_content', 'content']
        extra_kwargs = {'id': {'read_only': False, 'required': False}}

    def create(self, validated_data):
        content_data = validated_data.pop('content')
        content_type = validated_data.pop('type')
        content_instance = Content.objects.create(
            type=content_type, **validated_data)

        if content_type == 0:
            TextContent.objects.create(
                content=content_instance, text=content_data)
        elif content_type == 1:
            ImageContent.objects.create(
                content=content_instance, local_path=content_data)
        elif content_type == 2:
            AudioContent.objects.create(
                content=content_instance, local_path=content_data)

        return content_instance


class UserNoteSerializer(serializers.ModelSerializer):
    contents = ContentSerializer(many=True, required=False)

    class Meta:
        model = User_note
        fields = ['id', 'tag', 'title', 'created_at', 'updated_at', 'contents']

    def create(self, validated_data):
        contents_data = validated_data.pop('contents', [])
        user_note = User_note.objects.create(**validated_data)

        for content_data in contents_data:
            ContentSerializer().create({**content_data, 'note': user_note})

        return user_note

    def update(self, instance, validated_data):
        contents_data = validated_data.pop('contents', None)
        instance.title = validated_data.get('title', instance.title)
        instance.tag = validated_data.get('tag', instance.tag)
        instance.updated_at = validated_data.get(
            'updated_at', instance.updated_at)
        instance.save()

        if contents_data is not None:
            existing_content_ids = set(
                instance.contents.values_list('id', flat=True))

            for content_data in contents_data:
                content_id = content_data.get('id', None)

                if content_id and content_id in existing_content_ids:
                    content = Content.objects.get(id=content_id, note=instance)
                    content_type = content_data['type']
                    content_value = content_data['content']

                    if content_type == 0:
                        TextContent.objects.update_or_create(
                            content=content, defaults={'text': content_value})
                    existing_content_ids.remove(content_id)  # Mark as updated
                else:
                    ContentSerializer().create(
                        {**content_data, 'note': instance})

            # Delete any remaining contents that were not updated
            Content.objects.filter(id__in=existing_content_ids).delete()

        return instance
