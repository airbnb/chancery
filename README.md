# Chancery

Deploy when github is down! Chancery receives web hooks from Github, and copies gzipped tarballs of the references to an S3 bucket.

Based on Dropwizard, AWS SDK, and more (see `pom.xml`).

## Build it

    $ mvn package; ls -l target/chancery-1.0-SNAPSHOT.jar

## Prepare credentials

### Github

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

### AWS

1. Create an AMI user, keep its Access Key Id and Secret Access Key around.

2. Attach a user policy to it:

        {"Statement":[{
            "Action": ["s3:*"],
            "Effect": "Allow",
            "Resource": ["arn:aws:s3:::superstore.acme.com/repos/*"]
        }]}

## Set up Github web hooks

You'll need to create the Github web hooks with your real user, as the `acme-chancery` doesn't have sufficient rights.

    $ curl --silent --fail --request POST \
        -H 'Content-Type: application/json' \
        --data '{"name": "web", "active": true, "config": {"url": "https://chancery.ewr.corp.acme.com/callback", "content_type": "json"}}' \
        --user 'acme-root:god' \
        'https://api.github.com/repos/acme/project-manhattan/hooks'

## Write a configuration file

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

## Figure out what it does

Whenever a reference update is posted to `/callback`,
Chancery tries to match `$REPO_URI:$REF` against the `repoRefPattern` regular expression.

If it matches, the `CallbackPayload` object that contains all the callback information
is turned into a S3 object key by passing it through the `objectPathTemplate` MVEL 2
string expression.

The object at that key is either replaced by a new gzipped tarball of the reference,
or deleted if the reference was deleted.

A few things to note:

1. Refs are always in their canonical form:
  * A `v1.0` tag becomes `refs/tags/v1.0`
  * A `feature/cleartextpasswd` branch becomes `refs/heads/feature/cleartextpasswd`

2. A lot of information is usable in `objectPathTemplate`.
   Please look at the sources of `CallbackPayload` or Chancery logs to know more.

3. Chancery asks Github for the tarball corresponding to the commit ID provided indicated
   in the Github callback, not the reference name.

## Run it

    $  java -jar chancery-1.0-SNAPSHOT.jar server /etc/chancery.yml

## Monitor it

The various monitoring endpoints are listed on the admin port (8081 by default).

You'll a few basic health checks in `/healthcheck` and
a bunch of metrics in `/metrics?pretty`;
feel free to suggest more.

Please remember that the metrics are JMX-friendly;
the Dropwizard documentation is yet again of great help.

## Contribute

Open issues, send pull requests, share your love with
[@AirbnbNerds](https://twitter.com/AirbnbNerds) on Twitter!
