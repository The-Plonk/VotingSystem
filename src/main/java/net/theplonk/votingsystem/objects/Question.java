package net.theplonk.votingsystem.objects;

import redempt.redlib.config.annotations.ConfigPath;

public record Question(@ConfigPath String name, String title, String description) {


}
