
<#macro renderType part>
<#-- @ftlvariable name="part" type="com.comsysto.livingdoc.annotation.processors.plantuml.model.TypePart" -->
    <#if part.isInterface()>
        <#local typeDeclaration="interface">
    <#elseif part.isAbstract()>
        <#local typeDeclaration="abstract class">
    <#elseif part.isEnum()>
        <#local typeDeclaration="enum">
    <#else>
        <#local typeDeclaration="class">
    </#if>
${typeDeclaration} ${part.name}<#assign fields=part.getAnnotatedFields()><#if fields?has_content> {
<#list fields as field>
${simpleTypeName(field.asType())} ${field.simpleName}
</#list>
}
</#if>
    
    <#list part.notes as note>
note ${note.position()?lower_case} of ${part.name}
${note.body()}
end note

    </#list>
</#macro>

<#macro renderAssociation association>
    <#if association.relation="INHERITANCE">
        <#local relationOperator="<|--">
    <#elseif association.relation="RELAIZATION">
        <#local relationOperator="<|..">
    <#else>
        <#local relationOperator="-->">
    </#if>
${association.left.simpleName} ${relationOperator} ${association.right.simpleName}
</#macro>