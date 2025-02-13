import React from "react";
import { useFormState } from "react-hook-form";
import { FormattedMessage, useIntl } from "react-intl";
import { NavigateOptions, To, useNavigate } from "react-router-dom";
import * as yup from "yup";
import { SchemaOf } from "yup";

import { HeadTitle } from "components/common/HeadTitle";
import { Form, FormControl } from "components/forms";
import { Box } from "components/ui/Box";
import { Button } from "components/ui/Button";
import { FlexContainer } from "components/ui/Flex";
import { Heading } from "components/ui/Heading";
import { Link } from "components/ui/Link";
import { Text } from "components/ui/Text";

import { PageTrackingCodes, useTrackPage } from "core/services/analytics";
import { useAppMonitoringService } from "hooks/services/AppMonitoringService";
import { useNotificationService } from "hooks/services/Notification";
import { useQuery } from "hooks/useQuery";
import { CloudRoutes } from "packages/cloud/cloudRoutePaths";
import { useAuthService } from "packages/cloud/services/auth/AuthService";
import { LoginFormErrorCodes } from "packages/cloud/services/auth/types";

import styles from "./LoginPage.module.scss";
import { Disclaimer } from "../components/Disclaimer";
import { LoginSignupNavigation } from "../components/LoginSignupNavigation";
import { OAuthLogin } from "../OAuthLogin";
import { Separator } from "../SignupPage/components/Separator";

interface LoginPageFormValues {
  email: string;
  password: string;
}

const LoginPageValidationSchema: SchemaOf<LoginPageFormValues> = yup.object().shape({
  email: yup.string().email("form.email.error").required("form.empty.error"),
  password: yup.string().required("form.empty.error"),
});

const LoginButton: React.FC = () => {
  const { isSubmitting } = useFormState();

  return (
    <Button size="lg" type="submit" isLoading={isSubmitting} data-testid="login.submit">
      <FormattedMessage id="login.login" />
    </Button>
  );
};

export const LoginPage: React.FC = () => {
  const { formatMessage } = useIntl();
  const { login } = useAuthService();
  const { registerNotification } = useNotificationService();
  const { trackError } = useAppMonitoringService();

  const query = useQuery<{ from?: string }>();
  const navigate = useNavigate();
  const replace = (path: To, state?: NavigateOptions) => navigate(path, { ...state, replace: true });
  useTrackPage(PageTrackingCodes.LOGIN);

  const onSubmit = async (values: LoginPageFormValues) => {
    await login(values);
    return replace(query.from ?? "/");
  };

  const onError = (e: Error, { email }: LoginPageFormValues) => {
    trackError(e, { email });

    const errMsg = [
      LoginFormErrorCodes.EMAIL_NOT_FOUND,
      LoginFormErrorCodes.EMAIL_DISABLED,
      LoginFormErrorCodes.PASSWORD_INVALID,
      LoginFormErrorCodes.EMAIL_INVALID,
    ].includes(e.message as LoginFormErrorCodes)
      ? `login.${e.message}`
      : "errorView.unknownError";

    registerNotification({
      id: "login_error",
      text: formatMessage({
        id: errMsg,
      }),
      type: "error",
    });
  };

  return (
    <FlexContainer direction="column" gap="xl" className={styles.container}>
      <HeadTitle titles={[{ id: "login.login" }]} />
      <Heading as="h1" size="xl" color="blue">
        <FormattedMessage id="login.loginTitle" />
      </Heading>

      <OAuthLogin />
      <Separator />

      <Form<LoginPageFormValues>
        defaultValues={{
          email: "",
          password: "",
        }}
        schema={LoginPageValidationSchema}
        onSubmit={onSubmit}
        onError={onError}
      >
        <FormControl
          name="email"
          fieldType="input"
          type="text"
          label={formatMessage({ id: "login.yourEmail" })}
          placeholder={formatMessage({ id: "login.yourEmail.placeholder" })}
          autoComplete="email"
          data-testid="login.email"
        />
        <FormControl
          name="password"
          fieldType="input"
          type="password"
          label={formatMessage({ id: "login.yourPassword" })}
          placeholder={formatMessage({ id: "login.yourPassword.placeholder" })}
          autoComplete="current-password"
          data-testid="login.password"
        />

        <Box mt="2xl">
          <FlexContainer direction="row" justifyContent="space-between" alignItems="center">
            <Text size="sm" color="grey300">
              <Link to={CloudRoutes.ResetPassword} data-testid="reset-password-link">
                <FormattedMessage id="login.forgotPassword" />
              </Link>
            </Text>
            <LoginButton />
          </FlexContainer>
        </Box>
      </Form>
      <Disclaimer />
      <LoginSignupNavigation to="signup" />
    </FlexContainer>
  );
};
