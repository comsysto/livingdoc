<#-- @ftlvariable name="title" type="String" -->
<#-- @ftlvariable name="parts" type="java.util.List<com.comsysto.livingdoc.annotation.processors.plantuml.model.TypePart>" -->
<#import "types.part.ftl" as types>
@startuml
<#list getIncludeFiles() as includeFile>
!include ${includeFile}
</#list>
hide empty members
<#if title??>
title ${title}
</#if>
<#list parts as part>
    <@types.renderType part/>
</#list>

<#list getInheritanceRelations() as association>
    <@types.renderAssociation association/>
</#list>

<#list getAssociations() as association>
    <@types.renderAssociation association/>
</#list>
<#list getAdditionalRelations() as additionalRelation>
    ${additionalRelation.source.name} ..> ${additionalRelation.relation.target()}: ${additionalRelation.relation.description()}
</#list>

@enduml
