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
    <#assign fields=part.getAnnotatedFields()>
    <#assign methods=part.getAnnotatedMethods()>
    <#assign hasBody=fields?has_content || methods?has_content>
    <#if hasBody>
        
    </#if>
${typeDeclaration} ${part.name}<#if hasBody> {
    <#list fields as field>
    ${simpleTypeName(field.asType())} ${field.simpleName}
    </#list>

    <#list methods as method>
    ${simpleTypeName(method.returnType)} ${method.simpleName}(<#list method.parameters as parameter>${simpleTypeName(parameter.asType())} ${parameter.simpleName}<#if parameter?has_next>, </#if></#list>)
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
    <#elseif association.relation="REALIZATION">
        <#local relationOperator="<|..">
    <#else>
        <#local relationOperator="-->">
    </#if>
${association.left.simpleName} ${relationOperator} ${association.right.simpleName}
</#macro>