function *foo() {
    yield "一切有为法"
    yield "如梦幻泡影"
    yield "如露亦如电"
    yield "应作如是观"
}

function *applicationStart() {
    yield* foo()
}
