mod layout;
mod javagen;

fn main() {
    rsdl::driver::application_start(
        include_str!("stdlib.rsdl"),
        None,
        &[&javagen::JavaGenFactory()]
    );
}
