# Chancery

**Deploy when github is down!**

Chancery hears when you push to Github, and sends gzip'ped tarballs to an S3 bucket.
Based on Dropwizard, AWS SDK, lombok and MVEL2.

**Table of Contents**

- [What it does](#what-it-does)
- [Set it up (one-time operation)](#set-it-up-one-time-operation)
  - [Prepare credentials](#prepare-credentials)
    - [Github](#github)
    - [AWS](#aws)
  - [Build it](#build-it)
  - [Write a configuration file](#write-a-configuration-file)
- [Run it](#run-it)
- [Monitor it](#monitor-it)
- [Install a web hook (needed for every repository)](#install-a-web-hook-needed-for-every-repository)
- [Contribute](#contribute)

## What it does

Whenever someone updates a reference (tag, branch, etc.) in a monitored repository,
Github lets Chancery know through a web hook.

Chancery tries to match `$REPO_URI:$REF` against the `repoRefPattern`
regular expression in its configuration. This provides a limited but simple way
to specify which branches and/or tags of which repositories should get tarballed
on S3.

If the regular expression matches, the web hook message is passed to the
`objectPathTemplate` template from the configuration file.
This template turns it into an "object key" (think of it as the path of the
tarball in your bucket).

If the update deleted the reference, we delete the corresponding object;
if the update changed the commit the reference points to, we let Github
bake us a fresh gzip'ped tarball then put it there.

A few things to note:

1. References are always in their canonical form:
   a `v1.0` tag is `refs/tags/v1.0`,
   a `feature/cleartextpasswd` branch is `refs/heads/feature/cleartextpasswd`.

2. `objectPathTemplate` is a MVEL 2 string template.
   Please look at their [templating guide](http://mvel.codehaus.org/MVEL+2.0+Templating+Guide)
   for the syntax.
   A lot of information is usable in that template; please look at the sources
   of `CallbackPayload` or the Chancery logs to read those messages.

3. Chancery asks Github for the tarball corresponding to the commit ID provided indicated
   in the Github callback, not the reference name.

## Set it up (one-time operation)

### Prepare credentials

#### Github

1. Create an `acme-chancery` github user.

2. Create an oAuth2 token for that user, keep it around:

        $ curl --silent --fail --request POST \
            -H 'Content-Type: application/json' \
            --data '{"scopes":["repo"],"note":"chancery"}' \
            --user 'acme-chancery:password123' \
            https://api.github.com/authorizations
        {
          [...]
          "token": "1234567890abcdef1234567890abcdef12345678",
          [...]
        }

3. Create an bots team with "Pull only" permissions, give it access to the repositories to observe. Add `acme-chancery` to that team.

#### AWS

1. Create an AMI user, keep its Access Key Id and Secret Access Key around.

2. Attach a user policy to it:

        {"Statement":[{
            "Action": ["s3:*"],
            "Effect": "Allow",
            "Resource": ["arn:aws:s3:::superstore.acme.com/repos/*"]
        }]}

### Build it

You'll need Java 7 and a recent version of Maven (only tested with Maven 3).

    $ mvn package; ls -l target/chancery-1.0-SNAPSHOT.jar

### Write a configuration file

Dropwizard comes with many options, please refer to their documentation and
[commented example](https://github.com/codahale/dropwizard/blob/master/dropwizard-example/example.yml).

Here is a straightforward example for our story:

    awsAccessKeyID: XXXXXXXXXXXXXXXXXXXX
    awsSecretKey: YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY
    githubOauth2Token: 1234567890abcdef1234567890abcdef12345678
    
    # We'll explain those in the next section.
    bucketName: superstore.acme.com
    objectPathTemplate: repos/@{repository.name}/@{ref}
    repoRefPattern: "https://github.com/acme/project-(manhattan|brooklyn):refs/heads/prod/.*"
    
    githubHttpConfig:
      timeout: 5s
      connectionTimeout: 1s
      maxConnectionsPerRoute: 16
      keepAlive: 60s
    
    logging:
      console:
        enabled: true
        threshold: INFO

## Run it

You'll need Java 7, the `chancery-1.0-SNAPSHOT.jar` überjar and a configuration
file.

    $  java -jar chancery-1.0-SNAPSHOT.jar server /etc/chancery.yml

## Monitor it

The various monitoring endpoints are listed on the admin port (8081 by default).

You'll a few basic health checks in `/healthcheck` and
a bunch of metrics in `/metrics?pretty`;
feel free to suggest more.

Please remember that the metrics are JMX-friendly;
the Dropwizard documentation is yet again of great help.

## Install a web hook (needed for every repository)

You'll need to create the Github web hooks with your own user, as the
`acme-chancery` doesn't have sufficient rights.

    $ curl --silent --fail --request POST \
        -H 'Content-Type: application/json' \
        --data '{"name": "web", "active": true, "config": {
                   "url": "https://chancery.ewr.corp.acme.com/callback",
                   "content_type": "json"}}' \
        --user 'acme-root:god' \
        'https://api.github.com/repos/acme/project-manhattan/hooks'

Then verify that the Chancery configuration file, in particular `repoRefPattern`,
matches your particular needs.

## Contribute

Open issues, send pull requests, share your love with
[@AirbnbNerds](https://twitter.com/AirbnbNerds) on Twitter!
