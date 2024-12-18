import { System } from '../jvm'

export function *main(): Generator {
    System.out.println('Zdravstvuyte, mir!')

    yield '一切有为法'
    yield '如梦幻泡影'
    yield '如露亦如电'
    yield '应作如是观'
}

EntryPoint.register(main)