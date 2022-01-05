<#import "s0t-members.ftl" as Members>

<#macro renderTypes s0tModel>
<#-- @ftlvariable name="s0tModel" type="com.comsysto.livingdoc.s0t.model.S0tModel" -->
    <#list s0tModel.types?values as type>
        <@renderType type/>
    </#list>
</#macro>

<#macro renderType type>
<#-- @ftlvariable name="type" type="com.comsysto.livingdoc.s0t.model.TypeModel" -->
    <#if type.type == "INTERFACE">
        <#local typeDeclaration="interface">
    <#elseif type.type == "ABSTRACT">
        <#local typeDeclaration="abstract class">
    <#elseif type.type == "ENUM">
        <#local typeDeclaration="enum">
    <#else>
        <#local typeDeclaration="class">
    </#if>
    ${typeDeclaration} ${type.name.simpleName}<#assign fields=type.fields><#if fields?has_content> {
    <#list fields as field>
        <#if type.type == "ENUM" && field.type.name = type.name>
            ${field.name}
        <#else>
            <@Members.renderField field/>
        </#if>
    </#list>
    }
</#if>

    <#list type.notes as note>
        note ${note.position?lower_case} of ${type.name.simpleName}
        ${note.text}
        end note

    </#list>
</#macro>

