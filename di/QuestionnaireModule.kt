package com.example.presentations.questionnaire.di

import dagger.Binds
import dagger.Module
import dagger.Reusable
import com.example.base.BaseMapper
import com.example.presentations.questionnaire.data.dto.FieldDto
import com.example.presentations.questionnaire.data.dto.FileDto
import com.example.presentations.questionnaire.data.dto.FormDto
import com.example.presentations.questionnaire.data.dto.QuestionnaireDto
import com.example.presentations.questionnaire.data.dto.SettingDto
import com.example.presentations.questionnaire.data.dto.SubFieldDto
import com.example.presentations.questionnaire.data.mapper.FieldsMapper
import com.example.presentations.questionnaire.data.mapper.FileMapper
import com.example.presentations.questionnaire.data.mapper.FormMapper
import com.example.presentations.questionnaire.data.mapper.QuestionnaireMapper
import com.example.presentations.questionnaire.data.mapper.SettingMapper
import com.example.presentations.questionnaire.data.mapper.SubFieldMapper
import com.example.presentations.questionnaire.domain.entity.FileEntity
import com.example.presentations.questionnaire.domain.entity.FormEntity
import com.example.presentations.questionnaire.domain.entity.QuestionnaireEntity
import com.example.presentations.questionnaire.domain.entity.SettingEntity
import com.example.presentations.questionnaire.domain.entity.SubFieldEntity
import com.example.presentations.questionnaire.domain.fieldhandler.FieldHandler
import com.example.presentations.questionnaire.domain.fieldhandler.FieldHandlerImpl
import com.example.presentations.questionnaire.domain.fieldvalidator.FieldValidator
import com.example.presentations.questionnaire.domain.fieldvalidator.FieldValidatorImpl
import com.example.presentations.questionnaire.domain.models.FieldTypes
import com.example.presentations.questionnaire.domain.repository.QuestionnaireRepository
import com.example.presentations.questionnaire.domain.repository.QuestionnaireRepositoryImpl


@Module
interface QuestionnaireModule {

    @Binds
    @Reusable
    fun bindRepository(impl: QuestionnaireRepositoryImpl): QuestionnaireRepository

    @Binds
    @Reusable
    fun bindFieldValidator(impl: FieldValidatorImpl): FieldValidator

    @Binds
    @Reusable
    fun bindFieldAction(impl: FieldHandlerImpl): FieldHandler

    @Binds
    @Reusable
    fun bindQualificationMapper(impl: QuestionnaireMapper): BaseMapper<QuestionnaireDto, QuestionnaireEntity>

    @Binds
    @Reusable
    fun bindFromMapper(impl: FormMapper): BaseMapper<FormDto, FormEntity>

    @Binds
    @Reusable
    fun bindFieldsMapper(impl: FieldsMapper): BaseMapper<FieldDto, FieldTypes>

    @Binds
    @Reusable
    fun bindFileMapper(impl: FileMapper): BaseMapper<FileDto?, FileEntity?>

    @Binds
    @Reusable
    fun bindSettingMapper(impl: SettingMapper): BaseMapper<SettingDto, SettingEntity>

    @Binds
    @Reusable
    fun bindSubFieldMapper(impl: SubFieldMapper): BaseMapper<SubFieldDto, SubFieldEntity>
}