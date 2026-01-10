.PHONY: help build clean test publish sample check-updates format

GRADLEW := ./gradlew

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build the library
	$(GRADLEW) :eu-gryta-kmp-zebra:build

clean: ## Clean build artifacts
	$(GRADLEW) clean

test: ## Run tests
	$(GRADLEW) :eu-gryta-kmp-zebra:test

sample: ## Build sample app APK
	$(GRADLEW) :sample:assembleDebug

sample-release: ## Build sample app release APK
	$(GRADLEW) :sample:assembleRelease

install-sample: ## Build and install sample app to connected device
	$(GRADLEW) :sample:installDebug

publish: ## Publish library (GitHub Packages in CI, Maven Local otherwise)
ifdef GITHUB_ACTIONS
	$(GRADLEW) :eu-gryta-kmp-zebra:publishAllPublicationsToGitHubPackagesRepository --no-daemon --stacktrace --info --scan --no-configuration-cache
else
	$(GRADLEW) :eu-gryta-kmp-zebra:publishToMavenLocal
endif

check-updates: ## Check for dependency updates
	$(GRADLEW) dependencyUpdates

format: ## Format Kotlin code (currently no formatter configured)
	@echo "Kotlin code formatting not configured. Consider adding ktlint or detekt."

all: clean build test sample ## Clean, build, test, and build sample app
