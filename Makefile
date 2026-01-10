.PHONY: build clean publish sample

GRADLEW := ./gradlew

build:
	$(GRADLEW) :eu-gryta-kmp-zebra:build

clean:
	$(GRADLEW) clean

sample:
	$(GRADLEW) :sample:assembleDebug

install-sample:
	$(GRADLEW) :sample:installDebug

publish:
ifdef GITHUB_ACTIONS
	$(GRADLEW) :eu-gryta-kmp-zebra:publishAllPublicationsToGitHubPackagesRepository --no-daemon --stacktrace --info --scan --no-configuration-cache
else
	$(GRADLEW) :eu-gryta-kmp-zebra:publishToMavenLocal
endif